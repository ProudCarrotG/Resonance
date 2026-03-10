package com.resonance.aop;


import com.resonance.annotation.RequireHost;
import com.resonance.domain.Room;
import com.resonance.service.RoomService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class RequireHostAspect {

    @Autowired
    @Lazy
    private RoomService roomService;
    private static final Logger log = LoggerFactory.getLogger(RequireHostAspect.class);
    @Before("@annotation(com.resonance.annotation.RequireHost)")
    public void checkHostPermission(JoinPoint joinPoint){
        //1.获取被拦截方法的参数
        Object[] args = joinPoint.getArgs();

        if(args == null || args.length < 2){
            log.error("❌ 系统错误：@RequireHost 拦截到的方法参数缺失！");
            throw new RuntimeException("内部错误：校验方法参数错误");
        }
        String roomId = (String)args[0];
        String userId = (String)args[1];
        log.warn("⚠️ 拦截异常操作：用户 [{}] 试图操作不存在或已解散的房间 [{}]", userId, roomId);
        Room room = roomService.getRoom(roomId);

        if(room == null){
            throw new RuntimeException("操作失败，房间不存在或已解散");
        }


        if(!userId.equals(room.getHostId())){
            throw new RuntimeException("操作失败，只有房主才有权限操作");
        }

    }
}
