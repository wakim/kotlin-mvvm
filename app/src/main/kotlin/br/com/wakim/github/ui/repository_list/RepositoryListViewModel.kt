package br.com.wakim.github.ui.repository_list

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import br.com.wakim.github.data.RepositoryDataSource
import br.com.wakim.github.data.SchedulerProviderContract
import br.com.wakim.github.data.model.LCE
import br.com.wakim.github.data.model.NextPage
import br.com.wakim.github.data.model.RepositorySearchResponse
import br.com.wakim.github.util.addToCompositeDisposable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class RepositoryListViewModel @Inject constructor(private val repositoryDataSource: RepositoryDataSource,
                                                  schedulerProvider: SchedulerProviderContract) : ViewModel() {

    private val searchSubject = PublishSubject.create<Pair<String, NextPage>>().toSerialized()

    private var disposable = CompositeDisposable()
    private var currentQuery = ""

    private var currentPage = NextPage(true, 1)

    val repositoriesLiveData: LiveData<LCE<RepositorySearchResponse>> = MutableLiveData()

    init {
        searchSubject
                .observeOn(schedulerProvider.io)
                .doOnNext {
                    currentQuery = it.first
                    currentPage = it.second
                }
                .flatMap { repositoryDataSource.search(it.first, it.second) }
                .observeOn(schedulerProvider.ui)
                .subscribe(this::setValue)
                .addToCompositeDisposable(disposable)
    }

    fun bindSearch(searchObservable: Observable<String>) {
        searchObservable
                .filter { it.isNotBlank() }
                .map { it to currentPage.copy(page = 1) }
                .doOnNext(searchSubject::onNext)
                .subscribe()
                .addToCompositeDisposable(disposable)
    }

    fun bindPage(pageObservable: Observable<Any>) {
        pageObservable
                .filter { !currentQuery.isNullOrEmpty() }
                .map { currentQuery to currentPage.copy(page = currentPage.page + 1) }
                .doOnNext(searchSubject::onNext)
                .subscribe()
                .addToCompositeDisposable(disposable)
    }

    fun clearBindings() {
        disposable.clear()
    }

    private fun setValue(value: LCE<RepositorySearchResponse>) {
        (repositoriesLiveData as MutableLiveData).value = value
    }

    override fun onCleared() {
        super.onCleared()

        disposable.clear()
        searchSubject.onComplete()
    }
}