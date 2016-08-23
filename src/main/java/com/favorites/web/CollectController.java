package com.favorites.web;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.favorites.domain.Collect;
import com.favorites.domain.CollectRepository;
import com.favorites.domain.result.ExceptionMsg;
import com.favorites.domain.result.Response;
import com.favorites.service.CollectService;
import com.favorites.service.FavoritesService;
import com.favorites.utils.HtmlUtil;

@RestController
@RequestMapping("/collect")
public class CollectController extends BaseController{
	
	@Autowired
	private CollectRepository collectRepository;
	@Resource
	private FavoritesService favoritesService;
	@Resource
	private CollectService collectService;
	
	@RequestMapping(value="/changePrivacy/{id}/{type}")
	public String changePrivacy(Model model,@PathVariable("id") long id,@PathVariable("type") String type) {
		collectRepository.modifyById(type, id);
		logger.info("user info :"+getUser());
		return "home/standard";
	}
	
	/**
	 * 文章收集
	 * @param collect
	 * @return
	 */
	@RequestMapping(value = "/collect", method = RequestMethod.POST)
	public Response login(Collect collect) {
		logger.info("collect begin, param is " + collect);
		try {
			if(collectService.checkCollect(collect, getUserId())){
				collectService.saveCollect(collect, getUserId());
			}else{
				return result(ExceptionMsg.CollectExist);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("collect failed, ", e);
			return result(ExceptionMsg.FAILED);
		}
		return result();
	}
	
	/**
	 * 导入收藏夹
	 * @param path
	 */
	@RequestMapping("/import")
	public void importCollect(@RequestParam("htmlFile") MultipartFile htmlFile,Long favoritesId){
		logger.info("path:" + htmlFile.getOriginalFilename());
		if(null == favoritesId){
			logger.info("获取导入收藏夹ID失败："+ favoritesId);
			return;
		}
		try {
			List<String> urlList = HtmlUtil.importHtml(htmlFile.getInputStream());
			if(null == urlList || urlList.size() <= 0){
				logger.info("未获取到url连接");
				return ;
			}
			collectService.importHtml(urlList, favoritesId, getUserId());
		} catch (Exception e) {
			logger.error("导入html异常:",e);
		}
	}
	

	
	
}