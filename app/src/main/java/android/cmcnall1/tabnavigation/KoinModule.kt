package android.cmcnall1.tabnavigation

import org.koin.dsl.module.module

val koin = module {
    factory {
        MainPresenter(
            get()
        )
    }
    single{
        ChargePointRepo(
            get()
        )
    }
    single{
        ChargePointService()
    }
}