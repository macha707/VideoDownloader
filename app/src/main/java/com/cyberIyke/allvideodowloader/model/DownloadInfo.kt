package com.cyberIyke.allvideodowloader.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class DownloadInfo(var id: Int, val taskId: String, val name: String, val progress: Int, val line: String): Parcelable
