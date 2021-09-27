/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.contact.data.local.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.contact.data.local.db.entity.relation.toContact
import me.proton.core.contact.data.local.db.entity.relation.toContactWithCards
import me.proton.core.contact.data.local.db.entity.toContactCardEntity
import me.proton.core.contact.data.local.db.entity.toContactEmailEntity
import me.proton.core.contact.data.local.db.entity.toContactEmailLabel
import me.proton.core.contact.data.local.db.entity.toContactEntity
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.repository.ContactLocalDataSource
import me.proton.core.domain.entity.UserId

class ContactLocalDataSourceImpl(
    private val contactDatabase: ContactDatabase
): ContactLocalDataSource {

    override fun observeContact(contactId: ContactId): Flow<ContactWithCards> {
        return contactDatabase.contactDao().observeContact(contactId).map { it.toContactWithCards() }
    }

    override fun observeAllContacts(userId: UserId): Flow<List<Contact>> {
        return contactDatabase.contactDao().observeAllContacts(userId).map { entities ->
            entities.map { it.toContact() }
        }.distinctUntilChanged()
    }

    override suspend fun deleteContact(contactId: ContactId) {
        contactDatabase.contactDao().deleteContact(contactId)
    }

    override suspend fun deleteAllContacts(userId: UserId) {
        contactDatabase.contactDao().deleteAllContacts(userId)
    }

    override suspend fun deleteAllContacts() {
        contactDatabase.contactDao().deleteAllContacts()
    }

    private suspend fun mergeContact(userId: UserId, contact: Contact) {
        contactDatabase.inTransaction {
            contactDatabase.contactDao().insertOrUpdate(contact.toContactEntity(userId))

            contactDatabase.contactEmailDao().deleteAllContactsEmails(contact.id)
            mergeContactEmails(userId, contact.contactEmails)
        }
    }

    private suspend fun mergeContactEmails(userId: UserId, contactsEmails: List<ContactEmail>) {
        contactDatabase.inTransaction {
            contactDatabase.contactEmailLabelDao().deleteAllLabels(contactsEmails.map { it.id })

            val mailEntities = contactsEmails.map { it.toContactEmailEntity(userId) }
            contactDatabase.contactEmailDao().insertOrUpdate(*mailEntities.toTypedArray())

            val mailLabelEntities = contactsEmails.flatMap { it.toContactEmailLabel() }
            contactDatabase.contactEmailLabelDao().insertOrUpdate(*mailLabelEntities.toTypedArray())
        }
    }

    override suspend fun mergeContacts(userId: UserId, contacts: List<Contact>) {
        contactDatabase.inTransaction {
            contactDatabase.contactDao().deleteAllContacts(userId)
            contacts.forEach { mergeContact(userId, it) }
        }
    }

    override suspend fun mergeContactWithCards(userId: UserId, contactWithCards: ContactWithCards) {
        contactDatabase.inTransaction {
            mergeContact(userId, contactWithCards.contact)

            contactDatabase.contactCardDao().deleteAllContactCards(contactWithCards.id)
            val contactCardsEntities = contactWithCards.contactCards.map { it.toContactCardEntity(contactWithCards.id) }
            contactDatabase.contactCardDao().insertOrUpdate(*contactCardsEntities.toTypedArray())
        }
    }
}
