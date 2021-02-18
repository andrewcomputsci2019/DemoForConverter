
/*
 * Copyright (c) 2021. Andrew Pegg
 * Use as you will but do not rebrand my code as yours
 * Modify it as much as you need to do what you will
 */

package Main;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.EnhancedAttributeValue;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Tester {
    public Tester()
    {
        System.out.println("entered tester");
    }

    public void Connection()
    {
        DynamoDbClient client = DynamoDbClient.create();
        HashMap<String,AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("IDNumber", AttributeValue.builder().s("1").build());
        GetItemRequest request = GetItemRequest.builder().key(keyToGet).tableName("Students").build();
        try{
            Map<String, AttributeValue> returnItem = client.getItem(request).item();
            HashMap<String,Object> finalMap = new HashMap<>();
            for (String key1:returnItem.keySet()) {
                EnhancedAttributeValue value = EnhancedAttributeValue.fromAttributeValue(returnItem.get(key1));
                switch (value.type())
                {
                    case NULL -> finalMap.put(key1,null);
                    case BOOL -> finalMap.put(key1,value.asBoolean());
                    case N -> finalMap.put(key1,Double.parseDouble(value.asNumber()));
                    case S -> finalMap.put(key1,value.asString());
                    case L -> finalMap.put(key1,listConverter(value));
                    case M -> finalMap.put(key1,mapConverter(value));
                    case SS -> finalMap.put(key1,value.asSetOfStrings());
                    case NS -> finalMap.put(key1,value.asSetOfNumbers().stream().map(Double::parseDouble).collect(Collectors.toList()));
                    default -> System.out.println("unknown data type");
                }
            }
            System.out.println(finalMap);

        } catch (AwsServiceException e)
        {
            e.printStackTrace();
        }

    }
    //possibly going to be recursive because the fact this is going to parse json
    // no binary sets because idk dont feel like looking them up tbh
    private List<Object> listConverter(EnhancedAttributeValue passedValue)
    {
        assert passedValue !=null; // error checking
        LinkedList<Object> tempLink = new LinkedList<>(); // o(1) addition good for adding to a list will convert to array list at the end
        for (AttributeValue value: passedValue.asListOfAttributeValues()) {
            EnhancedAttributeValue enhancedAttributeValue = EnhancedAttributeValue.fromAttributeValue(value);
            switch (enhancedAttributeValue.type())
            {
                case S -> tempLink.add(enhancedAttributeValue.asString());
                case BOOL -> tempLink.add(enhancedAttributeValue.asBoolean());
                case N -> tempLink.add(Double.parseDouble(enhancedAttributeValue.asNumber()));
                case NULL -> tempLink.add(null);
                case SS -> tempLink.add(enhancedAttributeValue.asSetOfStrings());
                case NS -> tempLink.add(enhancedAttributeValue.asSetOfNumbers().stream().map(Double::parseDouble).collect(Collectors.toList()));
                case L -> tempLink.add(listConverter(enhancedAttributeValue));
                case M -> tempLink.add(mapConverter(enhancedAttributeValue));


            }
        }
       return (tempLink.size() > 0) ? new ArrayList<>(tempLink) : Collections.singletonList("empty");
    }

    private Map<String,Object> mapConverter(EnhancedAttributeValue valueMap)
    {
        assert valueMap !=null; //error checking
        HashMap<String,Object> finalMap = new HashMap<>();
        for (String key : valueMap.asMap().keySet())
        {
           AttributeValue temp = valueMap.asMap().get(key);
           EnhancedAttributeValue enhancedAttributeValue;
            enhancedAttributeValue = EnhancedAttributeValue.fromAttributeValue(temp);
            switch (enhancedAttributeValue.type())
           {
               case S -> finalMap.put(key,enhancedAttributeValue.asString());
               case BOOL -> finalMap.put(key,enhancedAttributeValue.asBoolean());
               case N -> finalMap.put(key,Double.parseDouble(enhancedAttributeValue.asNumber()));
               case NULL -> finalMap.put(key,null);
               case SS -> finalMap.put(key,enhancedAttributeValue.asSetOfStrings());
               case NS -> finalMap.put(key,enhancedAttributeValue.asSetOfNumbers().stream().map(Double::parseDouble).collect(Collectors.toList()));
               case L -> finalMap.put(key,listConverter(enhancedAttributeValue));
               case M -> finalMap.put(key,mapConverter(enhancedAttributeValue));


           }
        }
        return finalMap;
    }


}
