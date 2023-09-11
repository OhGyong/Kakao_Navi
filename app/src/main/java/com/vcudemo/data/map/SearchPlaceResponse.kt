package com.vcudemo.data.map

import com.google.gson.annotations.SerializedName

data class SearchPlaceResponse(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("documents") val documents: List<Document>
) {
    data class Meta(
        @SerializedName("totalCount") val totalCount: Int,
        @SerializedName("pageableCount") val pageableCount: Int,
        @SerializedName("isEnd") val isEnd: Boolean,
        @SerializedName("sameName") val sameName: SameName,
    ) {
        data class SameName(
            @SerializedName("region") val region: List<String>,
            @SerializedName("keyword") val keyword: String,
            @SerializedName("selectedRegion") val selectedRegion: String,
        )
    }

    data class Document(
        @SerializedName("id") val id: String,
        @SerializedName("placeName") val placeName: String,
        @SerializedName("categoryName") val categoryName: String,
        @SerializedName("categoryGroupCode") val categoryGroupCode: String,
        @SerializedName("categoryGroupName") val categoryGroupName: String,
        @SerializedName("phone") val phone: String,
        @SerializedName("addressName") val addressName: String,
        @SerializedName("roadAddressName") val roadAddressName: String,
        @SerializedName("x") val x: String,
        @SerializedName("y") val y: String,
        @SerializedName("placeUrl") val placeUrl: String,
        @SerializedName("distance") val distance: String,
    )
}