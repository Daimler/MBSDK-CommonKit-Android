package com.daimler.mbcommonkit.extensions

import android.os.Parcel

fun Parcel.writeBoolean(value: Boolean) = writeInt(if (value) 1 else 0)

fun Parcel.readBoolean(): Boolean = readInt() != 0