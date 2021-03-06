package com.nipun.oceanbin.core.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.nipun.oceanbin.core.PreferenceManager
import com.nipun.oceanbin.core.Resource
import com.nipun.oceanbin.feature_oceanbin.feature_news.presentation.components.NewsDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.nipun.oceanbin.R

class FireStoreManager(
    private val context: Context
) {

    private val preferenceManager = PreferenceManager(context)

    private val newsCollection = Firebase.firestore.collection("News")
    private val userCollection = Firebase.firestore.collection("Users")
    private val storageRef = FirebaseStorage.getInstance().reference
    private val mAuth = FirebaseAuth.getInstance()

    fun uploadImage(user: User, uri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading<String>())
        try {
            val imageRef = storageRef
                .child("Profiles")
                .child(user.id)
            imageRef.putFile(uri).await()
            val res = imageRef.downloadUrl.await()
            val updateUser = user.copy(image = res.toString())
            userCollection.document(user.id).set(updateUser).await()
            preferenceManager.saveUser(value = updateUser)
            emit(
                Resource.Success<String>(
                    data = context.getString(R.string.image_uploaded)
                )
            )
        } catch (e: Exception) {
            Log.e("Upload", e.message.toString())
            emit(
                Resource.Error<String>(
                    message = context.getString(R.string.failed_upload_image)
                )
            )
        }
    }

    fun updateUser(user: User) : Flow<Resource<String>> = flow {
        emit(Resource.Loading<String>())
        if(user.name.notValidName()){
            emit(
                Resource.Error<String>(
                    message = context.getString(R.string.not_valid_name)
                )
            )
            return@flow
        }
        if(user.phone.notValidPhone()){
            emit(
                Resource.Error<String>(
                    message = context.getString(R.string.not_valid_phone)
                )
            )
            return@flow
        }
        try {
            userCollection.document(user.id).set(user).await()
            preferenceManager.saveUser(value = user)
            emit(
                Resource.Success<String>(
                    data = context.getString(R.string.user_detail_update)
                )
            )
        } catch (e: Exception) {
            Log.e("Upload", e.message.toString())
            emit(
                Resource.Error<String>(
                    message = context.getString(R.string.failed_update_user)
                )
            )
        }
    }

    fun getNews(): Flow<Resource<List<NewsDetails>>> = flow {
        emit(Resource.Loading<List<NewsDetails>>())
        try {
            val result = newsCollection.get().await().toObjects(NewsDetails::class.java)
            emit(
                Resource.Success<List<NewsDetails>>(
                    data = result
                )
            )
        } catch (e: Exception) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<List<NewsDetails>>(
                    message = context.getString(R.string.something_went_wrong)
                )
            )
        }
    }

    fun createUser(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading<Boolean>())
        try {
            mAuth.createUserWithEmailAndPassword(email, password).await().user?.let { fUser ->
                val userId = fUser.uid
                fUser.sendEmailVerification().await()
                val user = User(
                    id = userId,
                    name = name,
                    email = email,
                    phone = phone,
                    image = "null"
                )
                userCollection.document(userId).set(user).await()
                mAuth.signOut()
                emit(
                    Resource.Success<Boolean>(
                        data = true
                    )
                )
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<Boolean>(
                    message = context.getString(R.string.user_already_exists)
                )
            )
        } catch (e: Exception) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<Boolean>(
                    message = context.getString(R.string.something_went_wrong)
                )
            )
        }
    }

    fun loginUser(
        email: String,
        password: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading<Boolean>())
        try {
            mAuth.signInWithEmailAndPassword(email, password).await().user?.let { fUser ->
                val userId = fUser.uid
                if (fUser.isEmailVerified) {
                    val user =
                        userCollection.document(userId).get().await().toObject(User::class.java)
                    user?.let { usr ->
                        preferenceManager.saveUser(value = user)
                        preferenceManager.saveBoolean(value = false)
                        emit(
                            Resource.Success<Boolean>(
                                data = true
                            )
                        )
                    } ?: emit(
                        Resource.Error<Boolean>(
                            message = context.getString(R.string.no_user_found)
                        )
                    )
                } else {
                    fUser.sendEmailVerification().await()
                    mAuth.signOut()
                    emit(
                        Resource.Error<Boolean>(
                            message = context.getString(R.string.email_verification_sent) + "$email\n" + context.getString(
                                R.string.open_link
                            )
                        )
                    )
                }
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<Boolean>(
                    message = context.getString(R.string.no_user_found)
                )
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<Boolean>(
                    message = context.getString(R.string.incorrect_credential)
                )
            )
        } catch (e: Exception) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<Boolean>(
                    message = context.getString(R.string.something_went_wrong)
                )
            )
        }
    }

    fun resetPassword(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading<String>())
        try {
            if (email.notValidEmail()) {
                emit(
                    Resource.Error<String>(
                        message = context.getString(R.string.invalid_mail)
                    )
                )
            } else {
                mAuth.sendPasswordResetEmail(email).await()
                emit(
                    Resource.Success<String>(
                        data = context.getString(R.string.password_verification_link) + " $email"
                    )
                )
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<String>(
                    message = context.getString(R.string.no_user_found)
                )
            )
        } catch (e: Exception) {
            Log.e("Nipun", e.message.toString())
            emit(
                Resource.Error<String>(
                    message = context.getString(R.string.something_went_wrong)
                )
            )
        }
    }
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val image: String = ""
)