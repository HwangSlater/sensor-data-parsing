package com.sensor_data_parsing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class ControlCenter {
        private static final String influxDBUrl = "http://192.168.71.210:8086"; // InfluxDB 서버 URL
        private static final String influxDBToken = "YKnMLcHEQ3wsyg778dUXYzvGijjmR4ImSVOYU2Nlr5BugSyFGjNsTyRb6c-5eozXGLTObSxNWqGW5yaUQxRaWw=="; // Token
        private static final String influxDBOrg = "nhnacademy_010"; // Organization 이름
        private static final String influxDBBucket = "test"; // 사용할 Bucket 이름

        private static final String mqttHost = "192.168.70.203"; // MQTT 브로커 주소
        private static final String mqttUsername = ""; // MQTT 사용자 이름
        private static final String mqttPassword = ""; // MQTT 비밀번호

        public static void main(String[] args) {
                // InfluxDB 클라이언트 설정
                InfluxDBClient influxDBClient = InfluxDBClientFactory.create(influxDBUrl, influxDBToken.toCharArray(),
                                influxDBOrg, influxDBBucket);

                // MQTT 클라이언트 설정
                final Mqtt5Client mqttClient = Mqtt5Client.builder()
                                .identifier("controlcenter-1234") // 클라이언트 식별자
                                .serverHost(mqttHost)
                                .automaticReconnectWithDefaultConfig() // 자동 재연결
                                .serverPort(1883)
                                .build();

                // MQTT 클라이언트 연결
                mqttClient.toBlocking().connectWith()
                                .simpleAuth()
                                .username(mqttUsername)
                                .password(mqttPassword.getBytes(StandardCharsets.UTF_8))
                                .applySimpleAuth()
                                .cleanStart(false)
                                .sessionExpiryInterval(TimeUnit.HOURS.toSeconds(1))
                                .send();

                // MQTT 토픽 구독 및 메시지 수신
                mqttClient.toAsync().subscribeWith()
                                .topicFilter("application/#") // 'application/'로 시작하는 모든 토픽 구독
                                .callback(publish -> {
                                        String message = new String(publish.getPayloadAsBytes(),
                                                        StandardCharsets.UTF_8);

                                        // JSON 파싱
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        try {
                                                JsonNode rootNode = objectMapper.readTree(message);
                                                JsonNode objectNode = rootNode.path("object");

                                                if (!objectNode.isMissingNode()) {
                                                        // deviceName과 spotName 추출
                                                        String deviceName = rootNode.path("deviceInfo")
                                                                        .path("deviceName").asText();
                                                        String spotName = rootNode.path("deviceInfo").path("tags")
                                                                        .path("name").asText();

                                                        System.out.println("deviceName: " + deviceName);
                                                        System.out.println("spotName: " + spotName);

                                                        // 데이터를 Map으로 변환
                                                        Map<String, Object> dataMap = objectMapper
                                                                        .readValue(objectNode.toString(), Map.class);

                                                        // 데이터 출력
                                                        System.out.println("----------data----------");
                                                        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                                                                System.out.println(entry.getKey() + ": "
                                                                                + entry.getValue());
                                                        }

                                                        // InfluxDB에 데이터를 저장
                                                        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                                                                String dataKey = entry.getKey();
                                                                Object dataValue = entry.getValue();

                                                                // Point 객체 생성
                                                                Point point = Point.measurement(dataKey)
                                                                                .addTag("deviceName", deviceName)
                                                                                .addTag("spotName", spotName);

                                                                // Field 추가 (타입에 맞춰서 저장)
                                                                if (dataValue instanceof Double) {
                                                                        point.addField("value", (Double) dataValue);
                                                                } else if (dataValue instanceof Integer) {
                                                                        point.addField("value", (Integer) dataValue);
                                                                } else if (dataValue instanceof String) {
                                                                        point.addField("value", (String) dataValue);
                                                                } else if (dataValue instanceof Boolean) {
                                                                        point.addField("value", (Boolean) dataValue);
                                                                } else if (dataValue instanceof Long) {
                                                                        point.addField("value", (Long) dataValue);
                                                                }

                                                                point.time(System.currentTimeMillis(),
                                                                                WritePrecision.MS); // 타임스탬프 설정
                                                                influxDBClient.getWriteApiBlocking().writePoint(point); // 데이터
                                                                                                                        // 삽입
                                                        }

                                                        System.out.println("데이터가 InfluxDB에 성공적으로 저장되었습니다!\n");
                                                }

                                        } catch (IOException e) {
                                                System.err.println("JSON 파싱 중 오류가 발생: " + e.getMessage());
                                        }

                                })
                                .send();

                // 클라이언트가 계속 실행되도록 대기
                System.out.println("MQTT 메시지를 기다리는 중...");
                while (true) {
                        // MQTT 클라이언트가 수신한 메시지 처리 대기
                }
        }
}
