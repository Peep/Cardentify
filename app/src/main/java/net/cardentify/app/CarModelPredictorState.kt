package net.cardentify.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Tora on 7/8/2017.
 */
class CarModelPredictorState(val predSimilarities: FloatArray?,
                             val predNames: Collection<String>?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createFloatArray(),
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloatArray(predSimilarities)
        parcel.writeStringList(predNames?.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CarModelPredictorState> {
        override fun createFromParcel(parcel: Parcel): CarModelPredictorState {
            return CarModelPredictorState(parcel)
        }

        override fun newArray(size: Int): Array<CarModelPredictorState?> {
            return arrayOfNulls(size)
        }
    }

}