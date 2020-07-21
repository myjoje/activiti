package com.myjoje.act;

import com.myjoje.util.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/act")
public class ActController {

    /**
     * 创建流程
     */
    @RequestMapping("/createFlow")
    public Object createFlow() {

        return Message.success();
    }


}
