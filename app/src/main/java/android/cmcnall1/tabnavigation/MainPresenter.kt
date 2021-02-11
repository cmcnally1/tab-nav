package android.cmcnall1.tabnavigation

class MainPresenter(private val chargePointRepo: ChargePointRepo): ChargePointRepo.Repository{

    private var view: View? = null

    init{
        chargePointRepo.repository = this
    }

    //When the view is created, it must attach to the presenter
    fun attachView(view: View){
        this.view = view
    }

    //You must detach from the presenter when the view is destroyed
    fun detachView() {
        this.view = null
    }

    fun getCurrentLocation(long: String, lat: String){
        chargePointRepo.getCurrentLocation(long, lat)
    }

    fun usePlaceLocation(long: String, lat: String){
        chargePointRepo.usePlaceLocation(long, lat)
    }


    override fun returnChargePoint(chargePoints: Array<PointInfo?>) {
        view?.showChargePoints(chargePoints)
    }

    interface View {
        fun showChargePoints(chargePoints: Array<PointInfo?>)
    }


}