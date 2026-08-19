package com.medibrary.api.bootstrap;

import com.medibrary.api.entity.Drug;
import com.medibrary.api.repository.DrugRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("local")
public class LocalSampleDataInitializer implements ApplicationRunner {
    private final DrugRepository drugRepository;

    public LocalSampleDataInitializer(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (drugRepository.count() > 0) {
            return;
        }
        drugRepository.saveAll(List.of(
                createDrug("LOCAL-001", "타이레놀정 500밀리그램", "원형", "하양", "TY", "500",
                        "Acetaminophen", "해열 및 감기에 의한 발열, 두통, 치통, 근육통 등의 통증 완화에 사용합니다.",
                        "성인은 1회 1~2정을 1일 3~4회 필요 시 복용합니다. 복용 간격은 4시간 이상으로 합니다.",
                        "다른 아세트아미노펜 함유 의약품과 중복 복용하지 마세요."),
                createDrug("LOCAL-002", "이부프로펜정 200밀리그램", "원형", "분홍", "IB", "200",
                        "Ibuprofen", "두통, 생리통, 치통, 근육통 및 염증성 통증의 완화에 사용합니다.",
                        "성인은 1회 1~2정을 필요 시 복용하며, 제품의 용법·용량을 확인하세요.",
                        "위장관 질환이나 신장 질환이 있거나 임신 중인 경우 복용 전 전문가와 상담하세요."),
                createDrug("LOCAL-003", "아스피린정 100밀리그램", "원형", "하양", "ASP", "100",
                        "Aspirin", "혈소판 응집을 억제하여 혈전 생성 위험을 낮추는 데 사용합니다.",
                        "의사 또는 약사가 안내한 용법·용량에 따라 복용하세요.",
                        "출혈 위험이 있거나 항응고제를 복용 중인 경우 반드시 의료진과 상담하세요.")
        ));
    }

    private Drug createDrug(String id, String name, String shape, String color, String markFront, String markBack,
                            String ingredientEn, String efficacy, String usageInfo, String caution) {
        Drug drug = new Drug(id, name);
        drug.setShape(shape);
        drug.setColor(color);
        drug.setMarkFront(markFront);
        drug.setMarkBack(markBack);
        drug.setIngredientEn(ingredientEn);
        drug.setEfficacy(efficacy);
        drug.setUsageInfo(usageInfo);
        drug.setCaution(caution);
        return drug;
    }
}
