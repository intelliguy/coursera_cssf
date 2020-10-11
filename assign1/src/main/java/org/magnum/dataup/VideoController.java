/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________  ___   ___  ___  ________  ___  __   
		|\   ____\|\   __  \|\   __  \|\   ___ \|\  \ |\  \|\  \|\   ____\|\  \|\  \ 
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \   \ \  \\ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \   \ \  \\ \  \\\  \ \  \\ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \   \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\   \ \_______\ \_______\ \_______\ \__\\ \__\
		\|_______|\|_______|\|_______|\|_______|\|_______|\|_______|\|_______|\|__| \|__|

	 * 
	 */

	public static final String VIDEO_PATH = "/video";
	public static final String VIDEO_DATA_PATH = VIDEO_PATH + "/{id}/data";
	private static final AtomicLong currentId = new AtomicLong(0L);
	private Map<Long,Video> videos = new HashMap<Long, Video>();
	
    private VideoFileManager videoDataMgr;


    public VideoController() {
    	try {
			// Initialize this member variable somewhere with
			// videoDataMgr = VideoFileManager.get()
			//

    		videoDataMgr = VideoFileManager.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
//	@RequestMapping(method = RequestMethod.GET, value = VIDEO_PATH)
//	public Video getVideoInfo (
//			@PathVariable("id") long id
//			) {
//		return videos.get(id);
//	}

	@RequestMapping(method = RequestMethod.GET, value = VIDEO_PATH)
	public @ResponseBody Collection<Video> getVideoList () {
	    return videos.values();
	}

	@RequestMapping(method = RequestMethod.POST, value = VIDEO_PATH)
	public @ResponseBody Video setVideoInfo(
			@RequestBody Video video,
			HttpServletRequest request
			) {
		checkAndSetId(video);
		video.setDataUrl(getUrlBaseForLocalServer(request)
				+ "/" + VIDEO_PATH + video.getId() + "/data");
		videos.put(video.getId(), video);
		return video;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = VIDEO_DATA_PATH)
	public void getVideo(
			@PathVariable("id") long id, 
			HttpServletResponse response
	) {
		try {
			videoDataMgr.copyVideoData(videos.get(id), response.getOutputStream());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = VIDEO_DATA_PATH)
	public @ResponseBody VideoStatus uploadVideo(
			@PathVariable("id") long id,
			@RequestParam("data") MultipartFile videoData,
			HttpServletResponse response
	) {
		try {
			videoDataMgr.saveVideoData(videos.get(id), videoData.getInputStream());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}

		return new VideoStatus(VideoState.READY);
	}
	
	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(currentId.incrementAndGet());
		}
	}
	
	private String getUrlBaseForLocalServer(HttpServletRequest request) {
		String base = 
				"http://"+request.getServerName() 
				+ ((request.getServerPort() != 80) ?
						":"+request.getServerPort() : "");
		return base;
	}
}
