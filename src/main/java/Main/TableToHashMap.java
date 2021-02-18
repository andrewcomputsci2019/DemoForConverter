/*
 * Copyright (c) 2021. Andrew Pegg
 * Use as you will but do not rebrand my code as yours
 * Modify it as much as you need to do what you will
 */

package Main;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.*;
import java.util.stream.Collectors;

public class TableToHashMap {
    public HashMap<String, AttributeValue> ValueToHash(HashMap<String,Object> objectHashMap)
    {
        HashMap<String,AttributeValue> finalMap = new HashMap<>();
        for (String key: objectHashMap.keySet())
        {
            Object temp = objectHashMap.get(key);
            if (temp == null)
            {
                finalMap.put(key, AttributeValue.builder().nul(true).build());
                continue;
            }
            if (!temp.getClass().isPrimitive()) {
                if (temp.getClass().isArray())
                {
                    temp= Arrays.stream((Object[])temp).collect((Collectors.toList()));
                }

                switch (temp.getClass().getName()) {
                    case "java.util.ArrayList": // needs to do a recursive call
                        ArrayList<Object> arrayList;
                        arrayList = (ArrayList<Object>) temp;
                        if (checkAllClass(arrayList))
                        {
                            Object className = arrayList.get(0).getClass().getName();
                            List<String> list = new LinkedList<>();
                            if (className.equals("java.lang.String"))
                            {
                                arrayList.forEach(obj-> list.add(obj.toString()));
                                finalMap.put(key, AttributeValue.builder().ss(list).build());
                            }
                            else{
                                arrayList.forEach(obj-> list.add(obj.toString()));
                                finalMap.put(key, AttributeValue.builder().ns(list).build());
                            }
                        }
                        else {
                            finalMap.put(key, AttributeValue.builder().l(listObjectToAttributeValue(arrayList)).build());
                        }
                        break;
                    case "java.util.LinkedList": // same here as Arraylist should make use of the list interface but oh well
                        LinkedList<Object> linkedList; // kinda deprecated code instead im using a function now to check if the class are the same
                        linkedList = (LinkedList<Object>) temp;
                        Object toCheck = linkedList.get(0);
                        if (toCheck != null){
                            String name = toCheck.getClass().getName();
                            if (name.equals("java.lang.String") || name.equals("java.lang.Integer") || name.equals("java.lang.Double")) {
                                boolean bool = linkedList.stream().allMatch(obj -> {
                                    if (obj != null) {
                                        return obj.getClass().getName().equals(name);
                                    } else
                                        return false;
                                });
                                if (bool) {
                                    LinkedList<String> converter = new LinkedList<>();
                                    if (name.equals("java.lang.String")) {
                                        linkedList.forEach(obj -> converter.add(obj.toString()));
                                        finalMap.put(key, AttributeValue.builder().ss(converter).build());
                                    } else {
                                        linkedList.forEach(obj -> converter.add(obj.toString()));
                                        finalMap.put(key, AttributeValue.builder().ns(converter).build());
                                    }
                                }
                            }
                                else
                                    finalMap.put(key, AttributeValue.builder().l(listObjectToAttributeValue(linkedList)).build());

                            }
                        else
                            finalMap.put(key, AttributeValue.builder().l(listObjectToAttributeValue(linkedList)).build());
                        break;
                    case "java.lang.String": // single cast to Attribute Value
                        System.out.println(temp);
                        finalMap.put(key, AttributeValue.builder().s(temp.toString()).build());
                        break;
                    case "java.lang.Integer","java.lang.Double":
                        finalMap.put(key, AttributeValue.builder().n(temp.toString()).build());
                        break;
                    case "java.lang.Boolean":
                        finalMap.put(key, AttributeValue.builder().bool((Boolean) temp).build());
                    case "java.util.HashMap", "java.util.LinkedHashMap", "java.util.Hashtable", "java.util.TreeMap":
                        finalMap.put(key, AttributeValue.builder().m(mapConverter((Map<String, Object>) temp)).build());
                        break;
                    default:
                           throw new IllegalStateException("Unexpected value: " + temp.getClass().getName());
                }
            }
            else
            {
                finalMap.put(key, AttributeValue.builder().s((String)temp).build());// just going to convert primitives to strings they can figure it out converting back
            }


        }

        return finalMap;
    }
    private List<AttributeValue> listObjectToAttributeValue(List<Object> objectList)
    {

        LinkedList<AttributeValue> attributeValues = new LinkedList<>();
        if (objectList == null) {
            return Collections.singletonList(AttributeValue.builder().nul(true).build());
        }

        for (Object object: objectList) {
            if (object == null)
            {
                attributeValues.add(AttributeValue.builder().nul(true).build());
                continue;
            }
            if (object.getClass().isArray())
            {
                object = Arrays.stream((Object[])object).collect((Collectors.toList()));
            }

                switch (object.getClass().getName()) {
                    case "java.util.ArrayList", "java.util.LinkedList" -> {
                        if (checkAllClass((List<Object>)object))
                        {
                            List<Object> objList = (List<Object>)object;
                            String name =  objList.get(0).getClass().getName();
                            if (name.equals("java.lang.String"))
                            {
                                List<String> stringList = new LinkedList<>();
                                objList.forEach(obj-> stringList.add(obj.toString()));
                                attributeValues.add(AttributeValue.builder().ss(stringList).build());
                            }
                            else
                            {
                                    List<String> numberList= new LinkedList<>();
                                    objList.forEach(obj-> numberList.add(obj.toString()));
                                    attributeValues.add(AttributeValue.builder().ns(numberList).build());
                            }
                        }
                        else {
                            attributeValues.add(AttributeValue.builder().l(listObjectToAttributeValue((List<Object>) object)).build());
                        }
                    }
                    case "java.lang.String" -> attributeValues.add(AttributeValue.builder().s(object.toString()).build());
                    case "java.lang.Integer","java.lang.Double" -> attributeValues.add(AttributeValue.builder().n(object.toString()).build());
                    case "java.util.HashMap", "java.util.LinkedHashMap", "java.util.Hashtable", "java.util.TreeMap" -> attributeValues.add(AttributeValue.builder().m(mapConverter((Map<String, Object>) object)).build());
                    case "java.lang.Boolean" -> attributeValues.add(AttributeValue.builder().bool((Boolean) object).build());
                    default -> System.out.println("Unknown data type of " + object.getClass().getName());
                }


        }

        return attributeValues;
    }

    private Map<String,AttributeValue> mapConverter(Map<String,Object> map)
    {
        HashMap<String,AttributeValue> finalMap = new HashMap<>();
        if ( map == null) {
            throw new NullPointerException("map type should not be null!");
        }

            for (String key: map.keySet()) {
                Object object = map.get(key);
                if (object == null)
                {
                    finalMap.put(key, AttributeValue.builder().nul(true).build());
                    continue;
                }
                if (object.getClass().isArray())
                {
                    object = Arrays.stream((Object[])object).collect((Collectors.toList()));
                }
                switch (object.getClass().getName()) {
                    case "java.util.ArrayList", "java.util.LinkedList" -> {
                        if (checkAllClass((List<Object>) object))
                        {
                            List<Object> objectList = (List<Object>) object;
                            String name = objectList.get(0).getClass().getName();
                            List<String> stringList = new LinkedList<>();
                            if (name.equals("java.lang.String"))
                            {
                                objectList.forEach(obj-> stringList.add(obj.toString()));
                                finalMap.put(key, AttributeValue.builder().ss(stringList).build());
                            }
                            else
                            {
                                objectList.forEach(obj-> stringList.add(obj.toString()));
                                finalMap.put(key, AttributeValue.builder().ns(stringList).build());
                            }
                        }

                        else
                            finalMap.put(key,AttributeValue.builder().l(listObjectToAttributeValue((List<Object>) object)).build());

                    }

                    case "java.lang.String" -> finalMap.put(key,AttributeValue.builder().s(object.toString()).build());
                    case "java.lang.Integer", "java.lang.Double" -> finalMap.put(key,AttributeValue.builder().n(object.toString()).build());
                    case "java.util.HashMap", "java.util.LinkedHashMap", "java.util.Hashtable", "java.util.TreeMap" -> finalMap.put(key, AttributeValue.builder().m(mapConverter((Map<String, Object>) object)).build());
                    case "java.lang.Boolean" -> finalMap.put(key,AttributeValue.builder().bool((Boolean) object).build());

                }
            }

        return finalMap;

    }
        private boolean checkAllClass(List<Object> passedList)
        {
            if (passedList == null) {
                return false;
            }
            else {
                    Object fistIndex = passedList.get(0);
                    if (fistIndex == null)
                    {
                        return false;
                    }
                    else
                    {
                        String nameOfClass = fistIndex.getClass().getName();
                        if (nameOfClass.equals("java.lang.String")||nameOfClass.equals("java.lang.Integer")||nameOfClass.equals("java.lang.Double"))
                        {
                            return passedList.stream().allMatch(obj -> {
                                if (obj != null) {
                                    return obj.getClass().getName().equals(nameOfClass);
                                } else
                                    return false;
                            });
                        }
                        else return false;

                    }


            }
        }



}


//@todo need to work on a map converted for conversion also need to add a way of just converting certain types like a ss knowing they just passed in strings or added a list into it same for ns with numbers
// think the predfine hashmap will be useful but also the idea of having a list converting and saving that back as just a string set or number set is also pretty benfical also need to add a null check
// as that can happen with object data type
