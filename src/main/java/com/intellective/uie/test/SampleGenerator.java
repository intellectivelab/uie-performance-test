package com.intellective.uie.test;

import com.intellective.commons.security.VuUserContext;
import com.intellective.commons.time.RadixDate;
import com.intellective.search.hli.v3.AllMatchCondition;
import com.intellective.search.hli.v3.BooleanCondition;
import com.intellective.search.hli.v3.ComparisonCondition;
import com.intellective.search.hli.v3.TestCondition;
import com.intellective.search.lli.v3.operator.Operator;
import com.intellective.search.lli.v3.operator.comparison.ComparisonOperator;
import com.intellective.uie.document.InputDocument;
import com.intellective.uie.transport.message.PagedSearchRequest;
import com.intellective.uie.transport.serde.json.UieJsonParser;

import java.io.FileOutputStream;
import java.util.Locale;

public class SampleGenerator {

    public static void main(String[] args) {

        PagedSearchRequest[] requests = new PagedSearchRequest[] {
                new PagedSearchRequest()
                        .add(new TestCondition(ComparisonOperator.STARTS)
                                .setFieldName(InputDocument.DOCUMENT_TITLE)
                                .setFieldValue("doc")
                                .addScope("SODemo"))
                        .addScope("SODemo")//.addScope("TestData")
                        .setToDocIndex(100),
                new PagedSearchRequest()
                        .add(new TestCondition(ComparisonOperator.STARTS)
                                .setFieldName("OTHER_ID@s")
                                .setFieldValue("33")
                                .addScope("SODemo"))
                        .addScope("SODemo")
                        .setToDocIndex(100),
                new PagedSearchRequest()
                        .add(new BooleanCondition(Operator.AND)
                            .add(new ComparisonCondition(Operator.GE)
                                .setFieldName(InputDocument.MODIFY_DATETIME)
                                .setFieldValue(new RadixDate(RadixDate.ISO_DATETIME_FORMATTER.parseDateTime("2001-12-03T07:00:08.768Z"))))
                            .add(new TestCondition(Operator.CONTAINS)
                                .setFieldName("CORR_NUMBER@s")
                                .setFieldValue("FILE")))
                        .setToDocIndex(100),
                new PagedSearchRequest()
                        .add(new BooleanCondition(Operator.AND)
                                .add(new ComparisonCondition(ComparisonOperator.RANGE)
                                        .setFieldName(InputDocument.MODIFY_DATE)
                                        .setFieldValue(new RadixDate[]{
                                                new RadixDate(RadixDate.ISO_DATETIME_FORMATTER.parseDateTime("2001-12-03T07:00:08.768Z")),
                                                new RadixDate(RadixDate.ISO_DATETIME_FORMATTER.parseDateTime("2020-12-03T07:00:08.768Z"))
                                        }))
                                .add(new TestCondition(Operator.CONTAINS)
                                        .setFieldName("CORR_COMPANY@s")
                                        .setFieldValue("Asso")))
                        .setToDocIndex(100)
        };

        for(int i = 0; i < requests.length; i++) {
            saveQObject("./src/main/resources/queries/query"+i+".json", UieJsonParser.getInstance().serialize(requests[i]));
        }

        VuUserContext userContext = new VuUserContext();
        userContext.getAccessRoles().add("Vega Unity Users");
        userContext.getAccessRoles().add("ega Unity Admins");
        userContext.getAccessRoles().add("testers");
        userContext.setLocale(Locale.US);


        saveQObject("./src/main/resources/users/user1.json", UieJsonParser.getInstance().serialize(userContext));

    }

    private static void saveQObject(String file, byte[] serialize) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(serialize);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
