简单的确认文件的模组，将 scanner.py 放在补丁文件夹下并运行，会生成一个 .json 文件，将其放入 config 文件夹中，在进入游戏时即可检查补丁内所有文件是否正确安装。    
* 扫描 example 目录下的所有文件    
python scanner.py example    
    
* 指定输出文件名    
python scanner.py example -o my_config.json     
    
* 包含文件哈希值（用于验证文件完整性）    
python scanner.py example --hash    
    
* 排除特定文件或目录    
python scanner.py example --exclude .git .backup temp    
    
* 生成独立的验证脚本    
python scanner.py example --verify-script    
