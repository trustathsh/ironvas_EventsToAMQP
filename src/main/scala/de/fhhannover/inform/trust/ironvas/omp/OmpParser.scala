/*
 * Project: ironvas
 * Package: main.scala.de.fhhannover.inform.trust.ironvas.omp
 * File:    OmpParser.scala
 *
 * Copyright (C) 2011-2012 Fachhochschule Hannover
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany 
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fhhannover.inform.trust.ironvas.omp

import java.util.Date
import java.util.GregorianCalendar

import scala.xml.Elem
import scala.xml.XML

import de.fhhannover.inform.trust.ironvas.Nvt
import de.fhhannover.inform.trust.ironvas.RiskfactorLevel
import de.fhhannover.inform.trust.ironvas.ThreatLevel
import de.fhhannover.inform.trust.ironvas.Vulnerability

/**
 * <code>OmpParser</code> is capable of parsing the OpenVAS Management Protocol
 * (OMP).
 * 
 * @author Ralf Steuerwald
 * 
 */
class OmpParser {
    
    /* Reminder: The "\" method on scala.xml.NodeSeq returns an empty NodeSeq
     * if the requested element does not exists, the same holds for attributes.
     * Example:
     *     val xml = <foo><bar>hello world</bar></foo>
     *     (xml \ "baz").text == ""
     * This means that none of the method in this class will throw an exception
     * because of an non existing element/attribute.
     */
    
    def getVersionResponse(xml: Elem) = {
        val statusCode = status(xml)
        val version = (xml \ "version").text
        
        (statusCode, version)
    }
    
    def getVersionResponse(xmlString: String): Tuple2[Tuple2[Int, String], String] = {
        val xml = XML.loadString(xmlString)
        getVersionResponse(xml)
    }
    
    def authenticateResponse(xml: Elem) = {
        status(xml)
    }
    
    def authenticateResponse(xmlString: String): Tuple2[Int, String] = {
        val xml = XML.loadString(xmlString)
        authenticateResponse(xml)
    }
    
    /**
     * Parse the response from a get_task request.
     * 
     * @param xml the XML to parse
     * @return a tuple with status information and a sequence of tasks
     */
    def getTasksResponse(xml: Elem) = {
        val statusCode = status(xml)
        
                    // iterate over all tasks elements in the document
        val tasks = for { task <- (xml \ "task")
            
        		id = (task \ "@id").text
        		name = (task \ "name").text
        		lastReportId = (task \ "last_report" \ "report" \ "@id").text
        } yield Task(id, name, lastReportId)
        
        (statusCode, tasks)
    }
    
    def getTasksResponse(xmlString: String): Tuple2[Tuple2[Int, String], Seq[Task]] = {
        val xml = XML.loadString(xmlString)
        getTasksResponse(xml)
    }
    
    /**
     * Parse the response from a get_report request.
     * 
     * @param xml the XML to parse
     * @return a tuple with status information and a sequence of vulnerabilities
     *         for each report that was fetched
     */
    def getReportsResponse(xml: Elem) = {
        val statusCode = status(xml)
        
                      // iterate over all reports in the document
        val reports = for { report <- (xml \ "report" \ "report")
            
            results = report \ "results"
            
            // iterate over all results in the current report
            vulnerabilities = for { result <- (results \ "result")
                
                // get the text values of the vulnerability elements
                id = (result \ "@id").text
                date = (report \ "scan_end").text
                subnet = (result \ "subnet").text
                host = (result \ "host").text
                port = (result \ "port").text
                threatLevel = (result \ "threat").text
                description = (result \ "description").text
                
                nvt = (result \ "nvt")
                
                // get the text values of the nvt elements
                nvt_oid = (nvt \ "@oid").text
                nvt_name = (nvt \ "name").text
                nvt_cvss_base = (nvt \ "cvss_base").text
                nvt_risk_factor = (nvt \ "risk_factor").text
                nvt_cve = (nvt \ "cve").text
                nvt_bid = (nvt \ "bid").text
                
            } yield { // process the values and finally yield the current vulnerability
                val base = parseCvssBase(nvt_cvss_base)
                val risk = parseRiskFactorLevel(nvt_risk_factor)
                val dateParsed = parseDate(date)
                val threat = parseThreatLevel(threatLevel)
                
            	val n = new Nvt(nvt_oid, nvt_name, base, risk, nvt_cve, nvt_bid)
            	new Vulnerability(id, dateParsed, subnet, host, port, threat, description, n)
            }
        } yield (vulnerabilities) // yield the collected vulnerability list
        
        (statusCode, reports)
    }
    
    def getReportsResponse(xmlString: String): ((Int, String), Seq[Seq[Vulnerability]]) = {
        val xml = XML.loadString(xmlString)
        getReportsResponse(xml)
    }
    
    /**
     * Parse the status informations from a response.
     * 
     * @param xml the XML to parse
     * @return a tuple with the status code an the status text
     */
    def status(xml: Elem) = {
        val status = (xml \ "@status").text
        val status_text = (xml \ "@status_text").text
        
        if (status == "" || status_text == "") {
            OmpProtocol.Extensions.parserError
        }
        else {
        	(status.toInt, status_text)
        }
    }
    
    def status(xmlString: String): (Int, String) = {
        val xml = XML.loadString(xmlString)
        status(xml)
    }
    
    
    val dateExtractorRE = """\w+\s+(\w+)\s+(\d{1,2})\s+(\d\d):(\d\d):(\d\d)\s+(\d\d\d\d)""".r
    
    /**
     * Convert a <code>ctime</code> date string into a <code>Date</code> object.
     * The date string contains no time zone information, it is assumed that
     * the default locale is valid for the source of the date string as well as
     * the target.
     * 
     * If the parsing fails the method returns January 1, 1970, 00:00:00 GMT.
     * 
     * @param dateString the date to parse
     * @return the <code>Date</code> object
     */
    def parseDate(dateString: String): Date = {
        // Example: "Wed Jun 30 21:49:08 1993"
        
        // TODO construct SimpleDateFormat with correct Locale to get rid of the regex
        dateString match {
            case dateExtractorRE(month, day, hour, minute, second, year) =>
                month match {
	                case "Jan" => new GregorianCalendar(year.toInt, 0, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Feb" => new GregorianCalendar(year.toInt, 1, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Mar" => new GregorianCalendar(year.toInt, 2, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Apr" => new GregorianCalendar(year.toInt, 3, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "May" => new GregorianCalendar(year.toInt, 4, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Jun" => new GregorianCalendar(year.toInt, 5, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Jul" => new GregorianCalendar(year.toInt, 6, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Aug" => new GregorianCalendar(year.toInt, 7, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Sep" => new GregorianCalendar(year.toInt, 8, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Oct" => new GregorianCalendar(year.toInt, 9, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Nov" => new GregorianCalendar(year.toInt, 10, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case "Dec" => new GregorianCalendar(year.toInt, 11, day.toInt, hour.toInt, minute.toInt, second.toInt).getTime()
	                case _ => {
	                    new Date(0)
	                }
            }
            case _ => {
                new Date(0)
            }
        }
    }
    
    /**
     * Parse a cvss_base string to a float number. If the string is empty or
     * "NONE" it is interpreted as <code>0.0f</code>.
     * If the string can't be parsed <code>-1.0f</code> is returned.
     * 
     * @param cvssBase the string to parse
     * @return a float 
     */
    def parseCvssBase(cvssBase: String) = {
        try {
            if (cvssBase == "None" || cvssBase == "") {
                0.0f
            }
            else {
                cvssBase.toFloat
            }
        } catch {
            case e: NumberFormatException => {
                -1.0f
            }
        }
    }
    
    def parseThreatLevel(level: String) = level match {
        case "High"   => ThreatLevel.High
        case "Medium" => ThreatLevel.Medium
        case "Low"    => ThreatLevel.Low
        case "Log"    => ThreatLevel.Log
        case "Debug"  => ThreatLevel.Debug
        case _        => ThreatLevel.Unknown
    }
    
    def parseRiskFactorLevel(level: String) = level match {
        case "Critical" => RiskfactorLevel.Critical
        case "High"     => RiskfactorLevel.High
        case "Medium"   => RiskfactorLevel.Medium
        case "Low"      => RiskfactorLevel.Low
        case "None"     => RiskfactorLevel.None
        case _          => RiskfactorLevel.Unknown
    }
    
}