package android.cmcnall1.tabnavigation

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.charge_point_row.view.*

class MainAdapter(private val chargePoints: Array<PointInfo?>): RecyclerView.Adapter<CustomViewHolder>(){

    // numberOfItems
    override fun getItemCount(): Int {
        return chargePoints.count()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {

        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.charge_point_row, p0, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(p0: CustomViewHolder, p1: Int) {

        val postcode = chargePoints[p1]?.AddressInfo?.Postcode
        val addressLine1 = chargePoints[p1]?.AddressInfo?.AddressLine1
        val distance = chargePoints[p1]?.AddressInfo?.Distance
        val price = chargePoints[p1]?.UsageCost
        p0.view.text_title.text = addressLine1.toString()
//        if (postcode == null){
//            p0.view.text_postcode.text = "No postcode available"
//        }else{
//            p0.view.text_postcode.text = postcode.toString()
//        }

        //Displays the postcode of the charge point
        p0.view.text_postcode.text = postcode


        //Converts the distance float into a string with 2 decimal points.
        p0.view.text_distance.text = "%.2f".format(distance) + " Miles"

        //click listener for selecting an item on the list of charge points
        p0.view.itemButton.setOnClickListener {
            p0.view.text_title.text = "Item Selected"
        }
    }
}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view){

}