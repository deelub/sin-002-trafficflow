package co.wethinkcode;

import za.co.wethinkcode.IngestionServiceApp;


class IntersectionLegacyTest {

    @ParametrizedTest
    @CsvFileSource(resources ="/intersections-legacy.csv", numLinesToskip=1)
    void MissingSignalTypeParameter(String intersectionID,String District,String signalType,boolean activeFlag){

        if (signalType ==null || signalType.trim().isEmpty()){
            Signalrecord record = new SignalRecord(
                    intersectionID.trim(),
                    district.trim(),
                    signalType.trim(),
                    "null"
            );

            assertEquals("null", record.getActiveFlag());
        }
    }

    @Test
    void IntersectionIdAllCaps{

    }

    @Test
    void RemoveRedundantIntersectionId {

    }

    @Test
    void MissingIntersectionId {

    }

    @Test
    void IncorrectIntersectionIdFormat{

    }

    @Test
    void SignalTypeCorrectFormat{

    }

    @Test
    void ActiveFlagCorrectFormat{

    }
}

