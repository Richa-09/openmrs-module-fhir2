/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PractitionerTranslatorUserImpl extends BasePractitionerTranslator implements PractitionerTranslator<User> {
	
	@Override
	public Practitioner toFhirResource(@Nonnull User user) {
		notNull(user, "The User object should not be null");
		
		Practitioner practitioner = new Practitioner();
		practitioner.setId(user.getUuid());
		
		Identifier userIdentifier = new Identifier();
		userIdentifier.setSystem(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER);
		userIdentifier.setValue(user.getSystemId());
		practitioner.addIdentifier(userIdentifier);
		
		if (user.getPerson() != null) {
			personToPractitioner(user.getPerson(), practitioner);
		}
		
		practitioner.getMeta().setLastUpdated(user.getDateChanged());
		
		return practitioner;
	}
	
	@Override
	public User toOpenmrsType(@Nonnull User user, @Nonnull Practitioner practitioner) {
		if (user == null) {
			return null;
		}
		
		if (practitioner == null) {
			return null;
		}
		
		user.setUuid(practitioner.getId());
		setSystemId(practitioner, user);
		
		if (user.getPerson() == null) {
			user.setPerson(new Person());
		}
		
		practitionerToPerson(practitioner, user.getPerson());
		
		user.setDateChanged(practitioner.getMeta().getLastUpdated());
		
		return user;
	}
	
	@Override
	public User toOpenmrsType(@Nonnull Practitioner practitioner) {
		notNull(practitioner, "The Practitioner object should not be null");
		return this.toOpenmrsType(new User(), practitioner);
	}
	
	private void setSystemId(Practitioner thePractitioner, User user) {
		thePractitioner.getIdentifier().forEach(practitioner -> {
			if (practitioner.hasSystem()) {
				if (practitioner.getSystem().equals(FhirConstants.OPENMRS_FHIR_EXT_USER_IDENTIFIER)) {
					user.setSystemId(practitioner.getValue());
				}
			}
		});
	}
}
