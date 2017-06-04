package br.com.wakim.github.injection

import br.com.wakim.github.ui.repository_list.RepositoryListViewModel

@Singleton
@Component(modules = arrayOf(AppModule::class, ApiModule::class, DataModule::class))
interface ApplicationComponent {
    fun inject(repositoryListViewModel: RepositoryListViewModel)
}