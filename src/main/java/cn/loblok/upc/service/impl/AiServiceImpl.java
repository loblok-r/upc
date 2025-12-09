package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.GenerateRequest;
import cn.loblok.upc.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Slf4j
public class AiServiceImpl implements AiService {

//    private static final String MOCK_BASIC_URL = "https://mock-cdn.com/avatar_basic.jpg";
//    private static final String MOCK_HD_URL = "https://mock-cdn.com/avatar_hd.jpg";
//    private static final String MOCK_PRO_URL = "https://mock-cdn.com/avatar_pro.jpg";
//
//    @Override
//    public AvatarResult generate(Long userId, GenerateRequest request) {
//        String plan = request.getPlan().toUpperCase();
//
//        String imageUrl, msg;
//        boolean commercial = false;
//
//        switch (plan) {
//            case "HD":
//                imageUrl = MOCK_HD_URL;
//                msg = "Mock: HD avatar generated";
//                break;
//            case "PRO":
//                imageUrl = MOCK_PRO_URL;
//                commercial = true;
//                msg = "Mock: PRO avatar generated (commercial allowed)";
//                break;
//            case "BASIC":
//            default:
//                imageUrl = MOCK_BASIC_URL;
//                msg = "Mock: BASIC avatar generated";
//                break;
//        }
//
//        return new AvatarResult()
//                .setTaskId("mock-" + System.currentTimeMillis())
//                .setImageUrl(imageUrl)
//                .setThumbnailUrl(imageUrl + "?x-oss-process=image/resize,w_100")
//                .setCommercialAllowed(commercial)
//                .setMessage(msg);
//    }
}