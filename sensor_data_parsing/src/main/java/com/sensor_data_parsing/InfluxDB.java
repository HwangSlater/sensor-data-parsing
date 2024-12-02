package com.sensor_data_parsing;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class InfluxDB {

    public static void main(String[] args) {
        // InfluxDB 2.x 연결 정보
        String url = "http://192.168.71.210:8086"; // InfluxDB 서버 URL
        String token = "YKnMLcHEQ3wsyg778dUXYzvGijjmR4ImSVOYU2Nlr5BugSyFGjNsTyRb6c-5eozXGLTObSxNWqGW5yaUQxRaWw=="; // Token
        String org = "nhnacademy_010"; // Organization 이름
        String bucket = "test"; // 사용할 Bucket 이름

        // InfluxDB 연결
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);

        // 예시: 데이터를 추가할 measurement 이름
        String measurement = "temperature";

        // 예시: 추출한 데이터 (각각 추출한 값들을 넣으세요)
        String deviceName = "DeviceA";
        String spotName = "Spot1";
        Object dataValue = 23.5;

        try {
            // Point 객체 생성 (measurement, tags, fields, timestamp)
            Point point = Point.measurement(measurement)
                    .addTag("deviceName", deviceName) // Tag
                    .addTag("spotName", spotName); // Tag

            // Field 값 타입 확인후 add
            if (dataValue instanceof Double) {
                point.addField("value", (Double) dataValue); // Double 값
            } else if (dataValue instanceof Integer) {
                point.addField("value", (Integer) dataValue); // Integer 값
            } else if (dataValue instanceof String) {
                point.addField("value", (String) dataValue); // String 값
            } else if (dataValue instanceof Boolean) {
                point.addField("value", (Boolean) dataValue); // Boolean 값
            } else if (dataValue instanceof Long) {
                point.addField("value", (Long) dataValue); // Long 값
            }

            point.time(System.currentTimeMillis(), WritePrecision.MS); // WritePrecision.MS 사용

            // 데이터 삽입
            influxDBClient.getWriteApiBlocking().writePoint(point);

            System.out.println("데이터가 InfluxDB에 성공적으로 저장되었습니다!");

        } catch (Exception e) {
            System.err.println("데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        } finally {
            influxDBClient.close(); // 연결 종료
        }
    }
}
