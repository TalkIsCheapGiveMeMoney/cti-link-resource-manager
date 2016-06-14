package com.tinet.ctilink.resourcemanager.util;

import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.util.SystemCmd;

import java.io.File;


/**
* 处理语音文件，包括删除，转换格式，添加。
*<p>
* 文件名： VoiceFile.java
*<p>
* Copyright (c) 2006-2010 T&I Net Communication CO.,LTD.  All rights reserved.
* @author 安静波
* @since 1.0
* @version 1.0
*/

public class VoiceFile {
	/**
	 * 转换公共语音上传文件
	 * @param srcFile 格式 383489384543.wav或 383489384543.mp3 383489384543为当前unix时间戳 文件名严格不能有空格
	 * @param destFile 格式383489384543.wav
	 * @return true：成功 false：失败
	 */
	public static boolean transferPublicVoice(String srcFile, String destFile) {
		//First: check whether srcFile==destFile or whether srcFile format is valid
		if (srcFile==null|| destFile ==null||srcFile.equals(destFile)){
			return false;
		}
		if (srcFile.indexOf(" ") != -1) {
			return false;
		}
		//Second: use sox command to transfer file format to 8k16bit wav file
		String cmd = "/usr/local/bin/sox -V6 " + Const.SOUNDS_IVR_VOICE_ABS_PATH + srcFile + " -c 1 -b 16 -r 8k -S "
				+ Const.SOUNDS_IVR_VOICE_ABS_PATH + destFile;
		String result = SystemCmd.executeCmd(cmd);
		//Third: check the return string if it has substring Finished transfer ok.
		if (result.indexOf("Done.") != -1) {
			//Final: execute delete command to remove source file
			if (mkDir(Const.SOUNDS_MOH_ABS_PATH + "/" + destFile)) {
				if(!srcFile.equals(destFile)){
					SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + srcFile);
				}
				SystemCmd.executeCmd("ln -s " + Const.SOUNDS_IVR_VOICE_ABS_PATH + destFile + " "
						+ Const.SOUNDS_MOH_ABS_PATH + destFile + "/" + destFile);
				return true;
			}
		}
		// delete the dest file
		SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + srcFile);
		if(!srcFile.equals(destFile)){
			SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + destFile);
		}
		SystemCmd.executeCmd("/bin/rm -rf " + Const.SOUNDS_MOH_ABS_PATH + destFile);
		return false;
	}

	/**
	 * 删除公共语音库文件
	 * @param file 格式 383489384543.wav文件名严格不能有空格
	 * @return true：成功 false：失败
	 */
	public static boolean deletePublicVoice(String file) {
		//First: check whether srcFile==destFile or whether srcFile format is valid
		if (file.indexOf(" ") != -1) {
			return false;
		}
		//Final: execute delete command to remove source file
		SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + file);
		SystemCmd.executeCmd("/bin/rm -rf " + Const.SOUNDS_MOH_ABS_PATH + file);
		return true;
	}

	/**
	 * 转换企业上传语音文件
	 * @param enterpriseId 企业号 例如：110001
	 * @param srcFile 格式 383489384543.wav或 383489384543.mp3 383489384543为当前unix时间戳 文件名严格不能有空格
	 * @param destFile 格式383489384543.wav
	 * @return true：成功 false：失败
	 */
	public static boolean transferEnterpriseVoice(Integer enterpriseId, String srcFile, String destFile) {
		//First: check whether srcFile==destFile or whether srcFile format is valid
		if (srcFile==null|| destFile ==null||srcFile.equals(destFile)){
			return false;
		}
		if (srcFile.indexOf(" ") != -1) {
			return false;
		}
		//Second: use sox command to transfer file format to 8k16bit wav file
		String cmd = "/usr/local/bin/sox -V6 " + Const.SOUNDS_IVR_VOICE_ABS_PATH + enterpriseId + "/" + srcFile
				+ " -c 1 -b 16 -r 8k -S " + Const.SOUNDS_IVR_VOICE_ABS_PATH + enterpriseId + "/" + destFile;
		String result = SystemCmd.executeCmd(cmd);

		//Third: check the return string if it has substring Finished transfer ok.
		if (result.indexOf("Done.") != -1) {
			if(!srcFile.equals(destFile)){
				SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + enterpriseId + "/" + srcFile);
			}
			return true;
		}
		// delete the dest file
		SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + enterpriseId + "/" + srcFile);
		if(!srcFile.equals(destFile)){
			SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + enterpriseId + "/" + destFile);
		}
		return false;
	}
	
	/**
	 * 软链企业传语音文件到等待语音
	 * @param enterpriseId 企业号 例如：110001
	 * @param dstFile 语音文件名
	 * @return true：成功 false：失败
	 */
	public static boolean linkEnterpriseVoiceMoh(Integer enterpriseId,String dstFile){
		//First: check whether srcFile==destFile or whether srcFile format is valid
		if (dstFile.indexOf(" ") != -1) {
			return false;
		}
		
		//Final: execute delete command to remove source file
		if (mkDir(Const.SOUNDS_MOH_ABS_PATH + enterpriseId + "/" + dstFile)) {
			SystemCmd.executeCmd("ln -s " + Const.SOUNDS_IVR_VOICE_ABS_PATH + enterpriseId + "/" + dstFile + " "
					+ Const.SOUNDS_MOH_ABS_PATH + enterpriseId + "/" + dstFile + "/" + dstFile);
			return true;
		}
		SystemCmd.executeCmd("/bin/rm -rf " + Const.SOUNDS_MOH_ABS_PATH + enterpriseId + "/" + dstFile);
		return false;
	}
	
	/**
	 * 删除等待语音软链
	 * @param path 路径
	 * @return true：成功 false：失败
	 */
	public static boolean unLinkEnterpriseVoiceMoh(String path) {
		//First: check whether srcFile==destFile or whether srcFile format is valid
		if (path.indexOf(" ") != -1) {
			return false;
		}
		//Final: execute delete command to remove source file
		SystemCmd.executeCmd("/bin/rm -rf " + path);
		return true;
	}

	/**
	 * 删除企业语音库文件
	 * @param path 格式 383489384543.wav文件名严格不能有空格
	 * @return true：成功 false：失败
	 */
	public static boolean deleteEnterpriseVoice(String path) {
		//First: check whether srcFile==destFile or whether srcFile format is valid
		if (path.indexOf(" ") != -1) {
			return false;
		}
		//Final: execute delete command to remove source file
		SystemCmd.executeCmd("/bin/rm -f " + Const.SOUNDS_IVR_VOICE_ABS_PATH + path);
		SystemCmd.executeCmd("/bin/rm -rf " + Const.SOUNDS_MOH_ABS_PATH + path);
		return true;
	}

	/**
	 * 创建新的目录 并赋予666权限
	 * @param path 目录地址
	 * @return true: 成功 false: 失败
	 */
	public static boolean mkDir(String path) {
		try {
			if (!(new File(path).isDirectory())) {
				boolean res = new File(path).mkdirs();
				SystemCmd.executeCmd("chmod -R 777 " + path);
				if (res) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Main method to test this class
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
