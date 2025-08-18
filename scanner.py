import os
import json
import hashlib
from pathlib import Path
from datetime import datetime

def calculate_file_hash(file_path, hash_algo='md5'):
    """
    计算文件的哈希值（可选功能，用于验证文件完整性）
    """
    hash_func = hashlib.new(hash_algo)
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(4096), b''):
            hash_func.update(chunk)
    return hash_func.hexdigest()

def get_file_info(file_path, base_path, include_hash=False):
    """
    获取单个文件的详细信息
    """
    relative_path = file_path.relative_to(base_path)
    path_str = str(relative_path).replace('\\', '/')
    
    # 获取文件扩展名
    ext = file_path.suffix.lower() if file_path.suffix else 'no_ext'
    
    # 确定文件类型
    file_type_map = {
        '.json': 'json',
        '.js': 'javascript',
        '.png': 'image',
        '.jpg': 'image',
        '.jpeg': 'image',
        '.gif': 'image',
        '.lang': 'language',
        '.txt': 'text',
        '.properties': 'properties',
        '.toml': 'toml',
        '.yml': 'yaml',
        '.yaml': 'yaml',
        '.xml': 'xml',
        '.snbt': 'nbt',
        '.nbt': 'nbt',
        '.mcmeta': 'mcmeta',
        '.ogg': 'audio',
        '.wav': 'audio',
        '.mp3': 'audio'
    }
    
    file_type = file_type_map.get(ext, 'other')
    
    file_info = {
        "path": path_str,
        "type": file_type,
        "extension": ext.lstrip('.') if ext != 'no_ext' else '',
        "size": file_path.stat().st_size,
        "required": True
    }
    
    # 可选：添加文件哈希值
    if include_hash:
        try:
            file_info["hash"] = calculate_file_hash(file_path)
        except Exception as e:
            file_info["hash"] = f"error: {str(e)}"
    
    return file_info

def scan_all_files(base_path, output_file="localization_checker.json", 
                   include_hash=False, exclude_patterns=None):
    """
    扫描目录下的所有文件并生成JSON配置
    
    Args:
        base_path: 要扫描的基础目录路径
        output_file: 输出的JSON文件名
        include_hash: 是否包含文件哈希值
        exclude_patterns: 要排除的文件模式列表（如 ['.git', '__pycache__']）
    """
    base_path = Path(base_path)
    file_list = []
    
    if not base_path.exists():
        print(f"错误：目录 {base_path} 不存在！")
        return None
    
    # 默认排除模式
    if exclude_patterns is None:
        exclude_patterns = ['.git', '__pycache__', '.idea', 'node_modules', '.DS_Store']
    
    # 使用os.walk遍历所有文件和子目录 <div class="liaobots-citations" index="0"></div><div class="liaobots-citations" index="2"></div><div class="liaobots-citations" index="3"></div>
    for root, dirs, files in os.walk(base_path):
        # 排除特定目录
        dirs[:] = [d for d in dirs if d not in exclude_patterns]
        
        root_path = Path(root)
        
        for file_name in files:
            # 排除特定文件
            if any(pattern in file_name for pattern in exclude_patterns):
                continue
                
            file_path = root_path / file_name
            
            try:
                file_info = get_file_info(file_path, base_path, include_hash)
                file_list.append(file_info)
            except Exception as e:
                print(f"处理文件 {file_path} 时出错: {e}")
    
    # 按路径排序文件列表
    file_list.sort(key=lambda x: x['path'])
    
    # 生成统计信息
    stats = generate_statistics(file_list)
    
    # 生成配置文件
    config = {
        "metadata": {
            "generated_at": datetime.now().isoformat(),
            "total_files": len(file_list),
            "total_size": sum(f['size'] for f in file_list),
            "source_directory": str(base_path.absolute())
        },
        "statistics": stats,
        "files": file_list,
        "successMessage": "§a[汉化检查] ✓ 汉化补丁已成功加载！共验证 {} 个文件".format(len(file_list)),
        "failureMessage": "§c[汉化检查] ✗ 汉化补丁未正确安装，请重新下载并安装！",
        "settings": {
            "checkOnStartup": True,
            "showDetailedErrors": True,
            "validateFileHash": include_hash
        }
    }
    
    # 写入JSON文件，使用缩进格式化 <div class="liaobots-citations" index="1"></div><div class="liaobots-citations" index="7"></div>
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(config, f, ensure_ascii=False, indent=2)
    
    print(f"\n✓ 已生成配置文件: {output_file}")
    print(f"  - 共扫描到 {len(file_list)} 个文件")
    print(f"  - 总大小: {format_file_size(sum(f['size'] for f in file_list))}")
    print(f"\n文件类型统计:")
    for file_type, count in stats['by_type'].items():
        print(f"  - {file_type}: {count} 个文件")
    
    return file_list

def generate_statistics(file_list):
    """
    生成文件统计信息
    """
    stats = {
        'by_type': {},
        'by_extension': {},
        'by_directory': {}
    }
    
    for file_info in file_list:
        # 按类型统计
        file_type = file_info['type']
        stats['by_type'][file_type] = stats['by_type'].get(file_type, 0) + 1
        
        # 按扩展名统计
        ext = file_info['extension']
        if ext:
            stats['by_extension'][ext] = stats['by_extension'].get(ext, 0) + 1
        
        # 按目录统计
        path_parts = file_info['path'].split('/')
        if len(path_parts) > 1:
            dir_name = path_parts[0]
            stats['by_directory'][dir_name] = stats['by_directory'].get(dir_name, 0) + 1
    
    return stats

def format_file_size(size_bytes):
    """
    格式化文件大小显示
    """
    for unit in ['B', 'KB', 'MB', 'GB']:
        if size_bytes < 1024.0:
            return f"{size_bytes:.2f} {unit}"
        size_bytes /= 1024.0
    return f"{size_bytes:.2f} TB"

def scan_with_pathlib(base_path, output_file="localization_checker.json"):
    """
    使用pathlib的rglob方法扫描所有文件（替代方案）
    这种方法更简洁，适合不需要复杂过滤的场景
    """
    base_path = Path(base_path)
    file_list = []
    
    # 使用rglob递归获取所有文件 <div class="liaobots-citations" index="4"></div><div class="liaobots-citations" index="9"></div><div class="liaobots-citations" index="13"></div>
    for file_path in base_path.rglob('*'):
        if file_path.is_file():
            file_info = get_file_info(file_path, base_path)
            file_list.append(file_info)
    
    # 生成配置
    config = {
        "files": file_list,
        "successMessage": f"§a[汉化检查] 已加载 {len(file_list)} 个汉化文件",
        "failureMessage": "§c[汉化检查] 汉化文件缺失"
    }
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(config, f, ensure_ascii=False, indent=2)
    
    return file_list

def create_verification_script(json_file="localization_checker.json", 
                              output_file="verify_files.py"):
    """
    生成一个独立的验证脚本，可以用来检查文件是否都存在
    """
    verification_script = '''#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
汉化文件验证脚本
自动生成于: {timestamp}
"""

import json
import sys
from pathlib import Path

def verify_localization_files(config_file, game_directory):
    """验证汉化文件是否都正确安装"""
    
    with open(config_file, 'r', encoding='utf-8') as f:
        config = json.load(f)
    
    game_path = Path(game_directory)
    missing_files = []
    verified_files = []
    
    for file_info in config.get('files', []):
        file_path = game_path / file_info['path']
        
        if file_path.exists():
            verified_files.append(file_info['path'])
        else:
            missing_files.append(file_info['path'])
    
    # 输出结果
    print(f"\\n验证结果:")
    print(f"✓ 已找到: {{len(verified_files)}} 个文件")
    print(f"✗ 缺失: {{len(missing_files)}} 个文件")
    
    if missing_files:
        print(f"\\n缺失的文件:")
        for file in missing_files[:10]:  # 只显示前10个
            print(f"  - {{file}}")
        if len(missing_files) > 10:
            print(f"  ... 还有 {{len(missing_files) - 10}} 个文件")
        return False
    else:
        print(f"\\n✓ 所有汉化文件都已正确安装！")
        return True

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("用法: python verify_files.py <游戏目录路径>")
        sys.exit(1)
    
    game_dir = sys.argv[1]
    config_file = "{config_file}"
    
    if verify_localization_files(config_file, game_dir):
        sys.exit(0)
    else:
        sys.exit(1)
'''.format(timestamp=datetime.now().isoformat(), config_file=json_file)
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(verification_script)
    
    print(f"✓ 已生成验证脚本: {output_file}")

# 主程序
if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='扫描汉化补丁文件并生成配置')
    parser.add_argument('directory', help='要扫描的汉化补丁目录')
    parser.add_argument('-o', '--output', default='localization_checker.json',
                       help='输出的JSON文件名 (默认: localization_checker.json)')
    parser.add_argument('--hash', action='store_true',
                       help='包含文件哈希值以验证完整性')
    parser.add_argument('--exclude', nargs='+',
                       help='要排除的文件/目录模式')
    parser.add_argument('--verify-script', action='store_true',
                       help='生成独立的验证脚本')
    
    args = parser.parse_args()
    
    # 执行扫描
    scan_all_files(
        args.directory,
        args.output,
        include_hash=args.hash,
        exclude_patterns=args.exclude
    )
    
    # 可选：生成验证脚本
    if args.verify_script:
        create_verification_script(args.output)