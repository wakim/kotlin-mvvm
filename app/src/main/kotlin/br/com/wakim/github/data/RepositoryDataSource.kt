package br.com.wakim.github.data

import br.com.wakim.github.data.model.LCE
import br.com.wakim.github.data.model.NextPage
import br.com.wakim.github.data.model.RepositorySearchResponse
import br.com.wakim.github.data.remote.Api

interface RepositoryDataSource {
    fun search(query: String, nextPage: NextPage): Observable<LCE<RepositorySearchResponse>>
}

class RepositoryRepository(val api: Api) : RepositoryDataSource {
    override fun search(query: String, nextPage: NextPage): Observable<LCE<RepositorySearchResponse>> {
        val page = nextPage.page

        return api.searchRepositories(query, page)
                .map {
                    it.body()?.copy(nextPage = if (it.hasMore()) NextPage(true, page + 1) else NextPage(false, page)) ?:
                            RepositorySearchResponse(emptyList(), NextPage(false, page))
                }
                .map { LCE(false, it, null) }
                .startWith(LCE(true, null, null))
                .onErrorReturn { t: Throwable -> LCE(false, null, t) }
    }
}