/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de
 * 
 * This file is part of ironvas, version 0.1.7, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2011 - 2016 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.hshannover.f4.trust.ironvas.utils;

import de.hshannover.f4.trust.ironvas.RiskfactorLevel;

/**
 * This converter maps the namespace of a Riskfactor in ironvas to the namespace of a Riskfactor in event pojos of
 * project clearer
 *
 * @author Marius Rohde
 *
 */

public class RiskfactorLevelConverter {

	public static de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel
			convertRiskfactorLevelToClearer(RiskfactorLevel riskfactorLevel) {
		// Unknown, None, Low, Medium, High, Critical
		switch (riskfactorLevel) {
			case Unknown:
				return de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel.Unknown;
			case None:
				return de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel.None;
			case Low:
				return de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel.Low;
			case Medium:
				return de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel.Medium;
			case High:
				return de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel.High;
			case Critical:
				return de.hshannover.f4.trust.ironevents.ironvas.common.RiskfactorLevel.Critical;
			default:
				return null;
		}

	}

}
