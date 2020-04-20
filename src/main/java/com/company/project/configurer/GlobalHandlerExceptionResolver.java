package com.company.project.configurer;

import com.alibaba.fastjson.JSON;
import com.company.project.core.Result;
import com.company.project.core.ResultCode;
import com.company.project.core.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhout
 * @date 2020/4/17 18:29
 */
public class GlobalHandlerExceptionResolver implements HandlerExceptionResolver {

  private final Logger logger = LoggerFactory.getLogger(WebMvcConfigurer.class);

  @Override
  public ModelAndView resolveException(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Result result = new Result();
    if (ex instanceof ServiceException) { // 业务失败的异常，如“账号或密码错误”
      logger.info(ex.getMessage());
      result.setCode(ResultCode.FAIL).setMessage(ex.getMessage());
    } else if (ex instanceof NoHandlerFoundException) {
      result.setCode(ResultCode.NOT_FOUND).setMessage("接口 [" + request.getRequestURI() + "] 不存在");
    } else {
      result
          .setCode(ResultCode.INTERNAL_SERVER_ERROR)
          .setMessage("接口 [" + request.getRequestURI() + "] 内部错误，请联系管理员");
      String message;
      if (handler instanceof HandlerMethod) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        message =
            String.format(
                "接口 [%s] 出现异常，方法：%s.%s，异常摘要：%s",
                request.getRequestURI(),
                handlerMethod.getBean().getClass().getName(),
                handlerMethod.getMethod().getName(),
                ex.getMessage());
      } else {
        message = ex.getMessage();
      }
      logger.error(message, ex);
    }
    responseResult(response, result);
    return new ModelAndView();
  }

  private void responseResult(HttpServletResponse response, Result result) {
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Content-type", "application/json;charset=UTF-8");
    response.setStatus(200);
    try {
      response.getWriter().write(JSON.toJSONString(result));
    } catch (IOException ex) {
      logger.error(ex.getMessage());
    }
  }
}
