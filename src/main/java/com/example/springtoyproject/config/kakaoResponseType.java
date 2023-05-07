package com.example.springtoyproject.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public enum kakaoResponseType {
    simpleText,
    simpleImage,
    BasicCard,
    CommerceCard,
    ListCard,
    ItemCard;


    public void getSimpleText(JSONObject output, String content) {
        output.put("simpleText", new JSONObject().put("text", content));
    }

    public void getSimpleImage(JSONObject output, String content) {
        output.put("simpleImage", new JSONObject().put("imageUrl", content));
    }

    public void getBasicCard(JSONObject output,JSONArray jsonArray) {

        JSONObject carousel = new JSONObject();
        JSONArray items = new JSONArray();
        carousel.put("type", "basicCard");
        carousel.put("items", items);

        for (int i = 0; i < Objects.requireNonNull(jsonArray).length(); i++) {
            JSONObject basicCard = new JSONObject();
            basicCard.put("title", jsonArray.getJSONObject(i).getString("SCHUL_NM"))
                    .put("description", jsonArray.getJSONObject(i).getString("ORG_RDNMA"))
                    .put("thumbnail", new JSONObject().put("imageUrl", "https://t1.kakaocdn.net/kakaocorp/about/OpenBuilder/builder_logo.png"))
                    .put("buttons",new JSONArray().put(new JSONObject().put("action", "block").put("label", "등록").put("blockId", "63ef5f200035284b215abadf").put("extra", jsonArray.getJSONObject(i)).put("messageText", jsonArray.getJSONObject(i).getString("SCHUL_NM"))));
            items.put(basicCard);
        }
        output.put("carousel", carousel);
    }

    public void setButtons(JSONArray jsonArray) {
        JSONObject object = new JSONObject();

        object.put("action", "block")
                .put("label", "등록")
                .put("blockId", "63ef5f200035284b215abadf");
//                .put("extra",jsonArray.getJSONObject(i))
//                .put("messageText", jsonArray.getJSONObject(i).getString("SCHUL_NM"));



    }

    public void setQuickReplies(JSONArray QuickReplies,String id,String...contents) {

        for (String content: contents) {
            JSONObject button = new JSONObject();

            button.put("label",content)
                    .put("action","block")
                    .put("messageText",content)
                    .put("blockId",id);

            QuickReplies.put(button);
        }
    }

}