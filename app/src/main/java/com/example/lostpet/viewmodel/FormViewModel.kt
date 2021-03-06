package com.example.lostpet.viewmodel

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import com.example.lostpet.itemAdapter.PictureItem
import com.example.lostpet.model.Animal
import com.example.lostpet.repository.AnimalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.MediaFile

class FormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimalRepository()

    enum class State {
        IS_SAVING,
        IS_FINISH,
        IDLE
    }

    val viewModelState = MutableLiveData<State>().apply {
        postValue(State.IDLE)
    }

    val genderList = MutableLiveData<List<String?>>().apply {
        postValue(null)
    }

    val pictureList = MutableLiveData<MutableList<PictureViewModel>>().apply {
        postValue(mutableListOf())
    }

    private var pictureUrl: ArrayList<String> = arrayListOf()

    val itemList = Transformations.map(pictureList) { picture ->
        picture.map {
            PictureItem(it)
        }
    }

    // -------------------------------------------------------------------

    fun displayGenderListFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            genderList.postValue(repository.getGender())
        }
    }

    val formTitle = MutableLiveData<String>()
    val formSpecies = MutableLiveData<String>()
    val formBreed = MutableLiveData<String>()
    val formAnimalName = MutableLiveData<String>()
    val formColor = MutableLiveData<String>()
    val formDate = MutableLiveData<String>()
    val formIdentificationNumber = MutableLiveData<String>()
    val formDescription = MutableLiveData<String>()
    val formGender = MutableLiveData<String>()
    val formLocation = MutableLiveData<String>()
    val userPhone = MutableLiveData<String>()
    var latitude: Double? = null
    var longitude: Double? = null
    var location: String? = null
    var postalCode: String? = null
    var state: String? = null
    var country: String? = null
    var city: String? = null
    var isLost: Boolean = true
    var animalId: String? = null

    val progressBarVisibility = Transformations.map(viewModelState) {
        if (it == State.IS_SAVING) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    val mediatorLiveData = MediatorLiveData<Boolean>().apply {
        addSource(pictureList) {
            postValue(isFormValid())
        }
        addSource(userPhone) {
            postValue(isFormValid())
        }
    }

    private fun isFormValid(): Boolean? {
        return pictureList.value?.isNotEmpty() ?: false &&
                userPhone.value != null
    }

    fun saveForm() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.postValue(State.IS_SAVING)
            pictureList.value?.map {
                repository.addPicturesToStorage(it.picture)?.forEach {
                    pictureUrl.add(it)
                }
            }
            if (animalId == null) {
                val animal = Animal(
                    "",
                    formGender.value,
                    formTitle.value,
                    formAnimalName.value,
                    formSpecies.value,
                    formBreed.value,
                    formIdentificationNumber.value,
                    formColor.value,
                    formDescription.value,
                    formDate.value,
                    location,
                    city,
                    postalCode,
                    country,
                    state,
                    latitude,
                    longitude,
                    true,
                    pictureUrl,
                    userPhone.value,
                    getCurrentUser()?.email,
                    getCurrentUser()?.uid
                )
                repository.addAnimal(animal)
            } else {
                updateHouse()
            }
            viewModelState.postValue(State.IS_FINISH)
        }
    }

    fun saveLostForm() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelState.postValue(State.IS_SAVING)
            pictureList.value?.map {
                repository.addPicturesToStorage(it.picture)?.forEach {
                    pictureUrl.add(it)
                }
            }
            val animal = Animal(
                "",
                formGender.value,
                formTitle.value,
                formAnimalName.value,
                formSpecies.value,
                formBreed.value,
                formIdentificationNumber.value,
                formColor.value,
                formDescription.value,
                formDate.value,
                formLocation.value,
                null,
                null,
                null,
                null,
                latitude,
                longitude,
                false,
                pictureUrl,
                userPhone.value,
                getCurrentUser()?.email,
                getCurrentUser()?.uid
            )
            repository.addAnimal(animal)
//            val animalId = repository.addAnimal(animal)
//            if (getCurrentUser() != null) {
//                repository.addUser(User(getCurrentUser()?.uid), animalId ?: "")
//            }
            viewModelState.postValue(State.IS_FINISH)
        }
    }

    private suspend fun updateHouse() {
        val animal = Animal(
            animalId ?: "",
            formGender.value,
            formTitle.value,
            formAnimalName.value,
            formSpecies.value,
            formBreed.value,
            formIdentificationNumber.value,
            formColor.value,
            formDescription.value,
            formDate.value,
            location,
            city,
            postalCode,
            country,
            state,
            latitude,
            longitude,
            true,
            arrayListOf(),
            userPhone.value,
            getCurrentUser()?.email,
            getCurrentUser()?.uid
        )
        if (animalId != null) {
//            repository.updateItem(animalId ?: "", animal)
            repository.testUpdate(
                animalId ?: "",
                formTitle.value ?: "",
                formSpecies.value ?: "",
                formBreed.value ?: "",
                formAnimalName.value ?: "",
                formColor.value ?: "",
                formIdentificationNumber.value ?: "",
                userPhone.value ?: "",
                formDescription.value ?: "",
                formGender.value ?: "",
                formDate.value ?: ""
            )
        }
    }

    fun getLoadData(animalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val animal = repository.getAnimalById(animalId)
            if (animal != null) {
                this@FormViewModel.animalId = animalId
                formTitle.postValue(animal.animalTitle)
                formSpecies.postValue(animal.species)
                formBreed.postValue(animal.breed)
                formAnimalName.postValue(animal.animalName)
                formColor.postValue(animal.color)
                formDate.postValue(animal.foundDate)
                formIdentificationNumber.postValue(animal.identificationNumber)
                formDescription.postValue(animal.description)
                formGender.postValue(animal.animalGender)
                userPhone.postValue(animal.userPhone)
                location = animal.location
                city = animal.city
                postalCode = animal.postalCode
                country = animal.country
                state = animal.state
                latitude = animal.latitude
                longitude = animal.longitude
            }
        }
    }

    fun addPhoto(photo: List<MediaFile>) {
        pictureList.postValue(pictureList.value?.union(photo.map {
            PictureViewModel(it.file.path)
        })?.toMutableList())
    }

    fun removePictures(position: Int) {
        pictureList.value?.removeAt(position)
        pictureList.postValue(pictureList.value)
    }

    private fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}
