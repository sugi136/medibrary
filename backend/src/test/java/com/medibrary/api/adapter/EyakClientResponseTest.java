package com.medibrary.api.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class EyakClientResponseTest {
    private EyakClient eyakClient;
    private Method extractDrugInformation;

    @BeforeEach
    void setUp() throws Exception {
        eyakClient = new EyakClient(
                "test-service-key",
                new ObjectMapper(),
                new ExternalRestClientFactory(1_000)
        );
        extractDrugInformation = EyakClient.class.getDeclaredMethod("extractDrugInformation", String.class);
        extractDrugInformation.setAccessible(true);
    }

    @Test
    void extractsDetailInformationFromResponseWrappedArray() throws Exception {
        ExternalDrugInformation information = extract("""
                {
                  "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL SERVICE"},
                    "body": {
                      "items": [{
                        "efcyQesitm": "해열 및 진통에 사용합니다.<br/>감기 증상 완화에 도움을 줍니다.",
                        "useMethodQesitm": "성인은 1회 1정을 복용합니다.",
                        "atpnWarnQesitm": "과다 복용하지 마세요.",
                        "atpnQesitm": "간질환이 있으면 상담하세요.",
                        "seQesitm": "발진, 구역 등이 나타날 수 있습니다."
                      }]
                    }
                  }
                }
                """);

        assertThat(information.available()).isTrue();
        assertThat(information.efficacy()).contains("해열 및 진통", "감기 증상");
        assertThat(information.usageInfo()).isEqualTo("성인은 1회 1정을 복용합니다.");
        assertThat(information.caution()).contains("과다 복용", "간질환");
        assertThat(information.sideEffects()).containsExactly("발진, 구역 등이 나타날 수 있습니다.");
    }

    @SuppressWarnings("unchecked")
    private ExternalDrugInformation extract(String responseBody) throws Exception {
        return (ExternalDrugInformation) extractDrugInformation.invoke(eyakClient, responseBody);
    }
}
