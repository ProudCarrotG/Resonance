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

@Aspect
@Component
public class RequireHostAspect {

    @Autowired
    @Lazy
    private RoomService roomService;

    @Before("@annotation(com.resonance.annotation.RequireHost)")
    public void checkHostPermission(JoinPoint joinPoint){

        //1.获取被拦截方法的参数

        Object[] args = joinPoint.getArgs();

        if(args == null || args.length < 2){
            throw new RuntimeException("内部错误：校验方法参数错误");
        }
        String roomId = (String)args[0];
        String userId = (String)args[1];

        Room room = roomService.getRoom(roomId);

        if(room == null){
            throw new RuntimeException("操作失败，房间不存在或已解散");
        }


        if(!userId.equals(room.getHostId())){
            throw new RuntimeException("操作失败，只有房主才有权限操作");
        }

    }
}
