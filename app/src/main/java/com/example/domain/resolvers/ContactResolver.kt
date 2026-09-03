package com.example.domain.resolvers

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.ContactInfo

class ContactResolver(private val context: Context) {

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun findContactsByName(nameQuery: String): List<ContactInfo> {
        if (!hasContactsPermission() || nameQuery.isBlank()) return emptyList()

        val results = mutableListOf<ContactInfo>()
        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        )

        val cleanQuery = nameQuery.trim()
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$cleanQuery%")

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.let {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val lookupIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)

                while (it.moveToNext()) {
                    val id = if (idIndex >= 0) it.getString(idIndex) ?: "" else ""
                    val name = if (nameIndex >= 0) it.getString(nameIndex) ?: "" else ""
                    val number = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""
                    val lookupKey = if (lookupIndex >= 0) it.getString(lookupIndex) else null

                    if (number.isNotBlank()) {
                        results.add(ContactInfo(id = id, name = name, phoneNumber = number.replace(" ", ""), lookupKey = lookupKey))
                    }
                }
            }
        } catch (e: Exception) {
            // Handle Contacts query errors gracefully
        } finally {
            cursor?.close()
        }

        return results.distinctBy { it.phoneNumber }
    }

    fun resolveCallerName(phoneNumber: String): String? {
        if (!hasContactsPermission() || phoneNumber.isBlank()) return null
        val cleanDigits = phoneNumber.filter { it.isDigit() }
        if (cleanDigits.length < 5) return null

        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val num = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""
                    val cleanNum = num.filter { d -> d.isDigit() }
                    if (cleanNum.endsWith(cleanDigits.takeLast(8)) || cleanDigits.endsWith(cleanNum.takeLast(8))) {
                        val name = if (nameIndex >= 0) it.getString(nameIndex) else null
                        if (!name.isNullOrBlank()) return name
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }
}
