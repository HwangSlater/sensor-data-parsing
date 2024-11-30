package com.sensor_data_parsing;

import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MqttSubscriber {
    private static final String DEFAULT_BROKER = "tcp://192.168.70.203:1883"; // 브로커 URL
    private static final String DEFAULT_TOPIC = "data/#"; // 메시지를 보낼 토픽
    private static final String DEFAULT_CLIENTID = "Subscriber"; // 클라이언트 ID

    public static void main(String[] args) {
        try (MqttClient client = new MqttClient(DEFAULT_BROKER, DEFAULT_CLIENTID)) {

            // 연결 설정
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true); // 클린 세션 사용

            // 메시지 수신 콜백 설정
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: "
                            + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // 장소 이름 추출
                    String placeName = topic.substring(topic.indexOf("/n/")).split("/")[2];
                    System.out.println("PlaceName: " + placeName);

                    // MQTT 메시지 페이로드를 문자열로 변환
                    String payload = new String(message.getPayload());

                    ObjectMapper objectMapper = new ObjectMapper();
                    // JSON 문자열을 Map으로 변환
                    Map<String, Object> dataMap = objectMapper.readValue(payload, Map.class);

                    printMap(dataMap);
                    System.out.println();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Message delivery complete: "
                            + token.getMessageId());
                }
            });

            // 브로커 연결
            System.out.println("Connecting to broker...");
            client.connect(options);
            System.out.println("Connected!");

            // 주제 구독
            System.out.println("Subscribing to topic: " + DEFAULT_TOPIC);
            client.subscribe(DEFAULT_TOPIC);

            // 10초 대기 후 종료
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                System.err.println("대기하는 중 interrupt발생" + e);
            }

            // 클라이언트 종료
            System.out.println("Disconnecting...");
            client.disconnect();
            System.out.println("Disconnected!");

        } catch (MqttException e) {
            System.err.println("Mqtt연결 중 exception발생" + e);
        }
    }

    private static Boolean isList = false;

    // Map을 출력하는 함수
    private static void printMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                System.out.println(key + ": ");

                for (Object item : (List<Object>) value) {
                    if (item instanceof Map) {
                        isList = true;
                        printMap((Map<String, Object>) item);
                    }
                }
            } else {
                if (Boolean.TRUE.equals(isList)) {
                    System.out.println(" - " + key + ": " + value);
                } else {
                    System.out.println(key + ": " + value);
                }
            }
        }

        isList = false;
    }
}
