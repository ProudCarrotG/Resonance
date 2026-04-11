package com.resonance.exception;

public class TokenInvalidException extends RuntimeException{
    // 构造方法 1：只传错误信息
    public TokenInvalidException(String message) {
        super(message); // 把消息交给父类 RuntimeException 去处理
    }

    // 构造方法 2：传错误信息，并且把底层的“案发现场（原始异常）”也一起带上
    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
