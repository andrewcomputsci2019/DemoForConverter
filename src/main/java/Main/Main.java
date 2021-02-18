
/*
 * Copyright (c) 2021. Andrew Pegg
 * Use as you will but do not rebrand my code as yours
 * Modify it as much as you need to do what you will
 */

package Main;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.EnhancedAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.InstantAsStringAttributeConverter;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;


import java.io.IOException;
import java.util.*;

public class Main {


    public static void main(String[] args)
    {
        DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        System.out.println("hello world");
        HashMap<String,AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("IDNumber", AttributeValue.builder().s("1").build());
        GetItemRequest request = GetItemRequest.builder().key(keyToGet).tableName("Students").build();


        try {
            Map<String, AttributeValue> returnItem = dynamoDbClient.getItem(request).item();
            if (returnItem != null) {
                System.out.println(returnItem.toString());
                System.out.println("Amazon DynamoDB table attributes: \n");
                System.out.println("keys: then value:");
                List<AttributeValue> value = returnItem.get("Assgiments").l();
                HashMap<String,String> finalmap = new HashMap<>();
                for (AttributeValue values:value) {
                   if (values.hasM())
                   {
                       Map<String,AttributeValue> map = values.m();
                       Iterator<String> keys = map.keySet().iterator();
                       for (AttributeValue attrbutes: map.values()) {
                           System.out.println(attrbutes);
                           finalmap.put(keys.next(), attrbutes.s());

                       }
                   }
                }
                System.out.println(finalmap);


            } else {
                System.out.format("No item found with the key %s!\n", "IDNumber: 1");
            }

        } catch (AwsServiceException | SdkClientException e) {
            e.printStackTrace();
        }

            Tester tester= new Tester();
        tester.Connection();
        Main issh = new Main();
        issh.upDateTable();
        issh.testForTable();
    }
    public void upDateTable()
    {
        //ScanRequest requester = ScanRequest.builder().tableName("Students").build();
        DynamoDbClient ddb = DynamoDbClient.create();
       // ScanResponse returnItem = ddb.scan(requester); // scan syntax used to get all items in db
       // System.out.println(returnItem.items().toString());
        HashMap<String,AttributeValue> itemValues = new HashMap<>();
        ArrayList<String> String = new ArrayList<>();
        String.add("String");
        String.add("String1");
        itemValues.put("IDNumber", AttributeValue.builder().s("4").build());
        itemValues.put("field1", AttributeValue.builder().ss(String).build());   // A=ss ["string1","string2","string3"]  // A=L[A=s: "string1" , A=s "string2"]
        itemValues.put("Name", AttributeValue.builder().s("John Doe").build());
        itemValues.put("Grade", AttributeValue.builder().n("11").build());
        PutItemRequest request = PutItemRequest.builder()
                .tableName("Students")
                .item(itemValues)
                .build();
        try {
            ddb.putItem(request);
            System.out.println("Students" +" was successfully updated");

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", "Students");
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    public void ScanTable()
    {
        DynamoDbClient ddb = DynamoDbClient.create();
        ScanRequest requester = ScanRequest.builder().tableName("Students").build();
        ScanResponse returnItem = ddb.scan(requester); // scan syntax used to get all items in db
        System.out.println(returnItem.items().toString()); // all items returned to user
    }
    public void testForTable()
    {
        HashMap<String,Object> innermap = new HashMap<>();
        LinkedList<Integer> linkedList = new LinkedList<>();
      TableToHashMap tableToHashMap = new TableToHashMap();
      HashMap<String, Object> hashMap = new HashMap<>();
      String[] array = new String[5];
      for (int i=0; i<5;i++)
      {
          array[i] = String.valueOf(i);
      }
      hashMap.put("StringArray",array);
     linkedList.add(12);
     linkedList.add(14);
     linkedList.add(null);
     innermap.put("Null",null);
     innermap.put("Linked",linkedList);
     hashMap.put("Map",innermap);
     HashMap<String,AttributeValue> converted = tableToHashMap.ValueToHash(hashMap);
     System.out.println(converted);

    }




}
