package com.androidhuman.example.simplegithub.ui.repo

import android.arch.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.data.remote.api.GithubApi
import com.androidhuman.example.simplegithub.data.remote.model.GithubRepoRemoteModel
import com.androidhuman.example.simplegithub.util.SupportOptional
import io.reactivex.subjects.BehaviorSubject

class RepositoryViewModel(val api: GithubApi) : ViewModel() {

    val repository: BehaviorSubject<SupportOptional<GithubRepoRemoteModel>> = BehaviorSubject.create()

    val message: BehaviorSubject<String> = BehaviorSubject.create()

    val isContentVisible: BehaviorSubject<Boolean>
            = BehaviorSubject.createDefault(false)

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()

//    fun requestRepositoryInfo(login: String, repoName: String): Disposable {
//        val repoObservable = if (!repository.hasValue()) {
//            api.getRepository(login, repoName)
//        } else {
//            Observable.empty()
//        }
//
//        return repoObservable
//                .doOnSubscribe { isLoading.onNext(true) }
//                .doOnTerminate { isLoading.onNext(false) }
//                .subscribeOn(Schedulers.io())
//                .subscribe({ repo ->
//                    repository.onNext(optionalOf(repo))
//                    isContentVisible.onNext(true)
//                }) {
//                    message.onNext(it.message ?: "Unexpected error")
//                }
//    }
}
