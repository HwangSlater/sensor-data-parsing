package com.sensor_data_parsing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

public class ControlCenter {

        public static void main(String[] args) {
                final String host = "192.168.70.203"; // 호스트 주소, '<알파벳+숫자>.s2.eu.hivemq.cloud' 형식으로 사용할 수 있음
                final String username = ""; // HiveMQ에서 생성한 사용자 이름
                final String password = ""; // HiveMQ에서 생성한 비밀번호

                // 1. MQTT 클라이언트 생성
                final Mqtt5Client client = Mqtt5Client.builder()
                                .identifier("controlcenter-1234") // 클라이언트 식별자 (고유한 ID 사용)
                                .serverHost(host) // MQTT 브로커 호스트 설정
                                .automaticReconnectWithDefaultConfig() // 클라이언트가 자동으로 재연결하도록 설정
                                .serverPort(1883) // MQTT 기본 포트 (일반적으로 1883, 보안 연결은 8883)
                                .build();

                // 2. 클라이언트 연결
                client.toBlocking().connectWith()
                                .simpleAuth() // 인증을 사용
                                .username(username) // 사용자 이름 설정
                                .password(password.getBytes(StandardCharsets.UTF_8)) // 비밀번호 설정
                                .applySimpleAuth() // 인증 정보 적용
                                .cleanStart(false) // 세션 유지 (false: 서버가 연결 상태를 유지)
                                .sessionExpiryInterval(TimeUnit.HOURS.toSeconds(1)) // 세션 만료 시간 설정 (1시간)
                                .send();

                // 3. 특정 토픽을 구독하고 메시지를 수신
                client.toAsync().subscribeWith()
                                .topicFilter("application/#") // 'application/'로 시작하는 모든 토픽 구독
                                .callback(publish -> {
                                        String message = new String(publish.getPayloadAsBytes(),
                                                        StandardCharsets.UTF_8);

                                        ObjectMapper objectMapper = new ObjectMapper();
                                        try {
                                                JsonNode rootNode = objectMapper.readTree(message);
                                                // deviceName 추출
                                                String deviceName = rootNode.path("deviceInfo").path("deviceName")
                                                                .asText();
                                                // spot 추출
                                                String spotName = rootNode.path("deviceInfo").path("tags").path("name")
                                                                .asText();
                                                System.out.println("deviceName: " + deviceName);
                                                System.out.println("spot: " + spotName);

                                                // data를 추출하기위해 object 추출
                                                JsonNode objectNode = rootNode.path("object");
                                                // dataName: dataValue를 맵에 저장
                                                Map<String, Object> dataMap = objectMapper
                                                                .readValue(objectNode.toString(), Map.class);

                                                // data 출력
                                                System.out.println("----------data----------");
                                                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                                                        System.out.println(entry.getKey() + ": " + entry.getValue());
                                                }
                                                System.out.println();
                                        } catch (IOException e) {
                                        }

                                })
                                .send();

                // 4. 메시지 발행
                // String topic = "test/topic";
                // String message = "temperature: 20";
                // client.toAsync().publishWith()
                // .topic(topic)
                // .payload(message.getBytes(StandardCharsets.UTF_8))
                // .send();
                // System.out.println("Message published to topic " + topic + ": " + message);
        }
}
