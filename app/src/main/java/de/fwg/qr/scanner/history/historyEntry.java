package de.fwg.qr.scanner.history;

import java.util.Date;

public class historyEntry {

    /**
     * The Id of the visited Station Base64Decoded into an 64bit int
     * 64^10 > 2^32 => int32 is too small, but even a signed long can store more information
     * congrats, you reduced saving space from 10 to 8 bytes.....
     */
    public String StationId;
    /**
     * Timestamp of the Moment when the QR-Code was scanned or precisely: when the history entry
     * created after the success of the scan
     */
    public Date TimeVisited;
    /**
     * The Name of the visited station which is gotten by requests, utilizing the Id
     */
    public String StationName;

    /**
     * Empty Constructor
     */
    public historyEntry(){

    }

    /**
     * Constructor for a newly created entry, where the time being used is the time of creation
     * @param stationId Id of the visited station as String, being converted to an Int
     */
    public historyEntry(String stationId){
        StationId = stationId;
        TimeVisited = new Date(); // Saves the Time the object was created into the Date object
    }

    /**
     * Constructor to build an already existent entry, read from the file, with a specific date
     * @param stationId Id of the visited station as an Long
     * @param timeVisited Time the station was visited
     */
    public historyEntry(String stationId, Date timeVisited){
        StationId = stationId;
        TimeVisited = timeVisited;
    }
}
