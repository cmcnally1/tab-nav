package android.cmcnall1.tabnavigation

class ChargePointRepo(private val chargePointService: ChargePointService): ChargePointService.DataListener {

    var repository: Repository? = null

    init {
        chargePointService.listener = this
    }

    fun getCurrentLocation(long: String, lat: String) {
        chargePointService.fetchJson(long, lat)
    }

    fun usePlaceLocation(long: String, lat: String) {
        chargePointService.fetchJson(long, lat)
    }

    interface Repository {
        fun returnChargePoint(chargePoints: Array<PointInfo?>)
    }

    override fun onHttpResponseReceived(response: Array<PointInfo?>) {
        repository?.returnChargePoint(response)
    }

}