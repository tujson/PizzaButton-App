package app.pizzabutton.android.common

import android.content.Context
import android.location.Geocoder
import android.util.Log
import app.pizzabutton.android.common.models.Store
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

private val TAG = PizzaFinder::class.java.simpleName
private const val DELIVERY_RANGE_MILES = 5

class PizzaFinder(private val applicationContext: Context) {

    private val placesClient: PlacesClient

    init {
        Places.initialize(
            applicationContext,
            BuildConfig.PLACES_API_KEY
        )
        placesClient = Places.createClient(applicationContext)
    }

    fun getNearestPizza(userAddress: String, onFindNearestPizza: (Store?) -> Unit) {
        val userLatLng = getLatLng(userAddress)
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setOrigin(userLatLng)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setSessionToken(token)
            .setQuery("Pizza")
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val validPredictions = mutableListOf<AutocompletePrediction>()

            response.autocompletePredictions.forEach { prediction ->
                prediction.distanceMeters?.div(1609.0)?.let { distanceMiles ->
                    if (distanceMiles <= DELIVERY_RANGE_MILES) {
                        validPredictions.add(prediction)
                    }
                }
            }

            if (validPredictions.size == 0) {
                onFindNearestPizza(null)
            } else {
                // TODO: Come up with a better way to find best prediction, e.g. prefer known pizza places
                val bestPredictionPlaceId = validPredictions[0].placeId
                validPredictions.forEach {
                    Log.v(TAG, "Prediction: ${it.getFullText(null)}")
                }

                val placeFields =
                    listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER)
                val fetchPlaceRequest =
                    FetchPlaceRequest.newInstance(bestPredictionPlaceId, placeFields)
                placesClient.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener {
                        val bestPlace = it.place
                        if (bestPlace.name != null && bestPlace.address != null && bestPlace.phoneNumber != null) {
                            onFindNearestPizza(
                                Store(
                                    bestPlace.name!!,
                                    bestPlace.address!!,
                                    bestPlace.phoneNumber!!
                                )
                            )
                        } else {
                            onFindNearestPizza(null)
                        }
                    }.addOnFailureListener {
                        Log.e(TAG, "Fetch Place for $bestPredictionPlaceId place id.", it)
                        onFindNearestPizza(null)
                    }
            }
        }.addOnFailureListener {
            Log.e(TAG, "Autocomplete failed", it)
        }
    }

    private fun getLatLng(userAddress: String): LatLng {
        val geocoder = Geocoder(applicationContext)
        val addressLocation = geocoder.getFromLocationName(userAddress, 1)[0]
        return LatLng(addressLocation.latitude, addressLocation.longitude)
    }
}