/**
 * TruckTools 原型图交互脚本
 * 卡车外贸综合工具平台 - MVP版本
 */

// 页面内容模板
const pageTemplates = {
    // 名片识别与录入页面
    'card-upload': `
        <div class="page-header">
            <div>
                <h1 class="page-title">名片识别与录入</h1>
                <p class="page-subtitle">上传名片图片，AI自动识别并提取客户信息</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-secondary">📥 下载模板</button>
                <button class="btn btn-primary">📤 批量上传</button>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">📇</div>
                <div class="stat-value">156</div>
                <div class="stat-label">累计识别名片</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">✅</div>
                <div class="stat-value">92.5%</div>
                <div class="stat-label">平均识别准确率</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">⏱️</div>
                <div class="stat-value">2.3s</div>
                <div class="stat-label">平均识别耗时</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🕐</div>
                <div class="stat-value">12</div>
                <div class="stat-label">待处理名片</div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">上传名片</h3>
            </div>
            <div class="upload-zone" onclick="simulateUpload()">
                <div class="upload-icon">📷</div>
                <div class="upload-title">点击或拖拽名片图片到此区域</div>
                <div class="upload-hint">支持 JPG, PNG, HEIC 格式，单张不超过 10MB，最多同时上传 50 张</div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">待处理名片</h3>
                <button class="btn btn-primary btn-sm">全部确认转为客户</button>
            </div>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;">
                ${generateCardItems()}
            </div>
        </div>
    `,

    // 客户信息表格页面
    'customer-list': `
        <div class="page-header">
            <div>
                <h1 class="page-title">客户信息表格</h1>
                <p class="page-subtitle">管理所有客户信息，支持筛选、排序和导出</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-secondary">📥 导出Excel</button>
                <button class="btn btn-primary" onclick="showPage('card-upload')">➕ 添加客户</button>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">👥</div>
                <div class="stat-value">1,256</div>
                <div class="stat-label">客户总数</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">⭐</div>
                <div class="stat-value">89</div>
                <div class="stat-label">高优先级客户</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">📧</div>
                <div class="stat-value">856</div>
                <div class="stat-label">本月邮件发送</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🌍</div>
                <div class="stat-value">42</div>
                <div class="stat-label">覆盖国家数</div>
            </div>
        </div>

        <div class="card">
            <div class="filter-bar">
                <div class="filter-item">
                    <label>优先级</label>
                    <select>
                        <option>全部</option>
                        <option>⭐⭐⭐⭐⭐ 最高</option>
                        <option>⭐⭐⭐⭐ 高</option>
                        <option>⭐⭐⭐ 中</option>
                        <option>⭐⭐ 低</option>
                        <option>⭐ 最低</option>
                    </select>
                </div>
                <div class="filter-item">
                    <label>国家</label>
                    <select>
                        <option>全部</option>
                        <option>美国</option>
                        <option>德国</option>
                        <option>日本</option>
                        <option>英国</option>
                        <option>法国</option>
                    </select>
                </div>
                <div class="filter-item">
                    <label>来源</label>
                    <select>
                        <option>全部</option>
                        <option>名片识别</option>
                        <option>Excel导入</option>
                        <option>手动录入</option>
                    </select>
                </div>
                <div class="filter-item">
                    <label>关键词</label>
                    <input type="text" placeholder="搜索姓名、公司、邮箱...">
                </div>
                <button class="btn btn-outline" style="margin-left: auto;">🔍 搜索</button>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th><input type="checkbox"></th>
                        <th>优先级</th>
                        <th>客户姓名</th>
                        <th>邮箱</th>
                        <th>公司</th>
                        <th>国家</th>
                        <th>会面时间</th>
                        <th>来源</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    ${generateCustomerRows()}
                </tbody>
            </table>

            <div class="pagination">
                <div class="pagination-info">显示 1-20 条，共 1,256 条记录</div>
                <div class="pagination-buttons">
                    <button class="pagination-btn">上一页</button>
                    <button class="pagination-btn active">1</button>
                    <button class="pagination-btn">2</button>
                    <button class="pagination-btn">3</button>
                    <button class="pagination-btn">...</button>
                    <button class="pagination-btn">63</button>
                    <button class="pagination-btn">下一页</button>
                </div>
            </div>
        </div>
    `,

    // Excel批量导入页面
    'excel-import': `
        <div class="page-header">
            <div>
                <h1 class="page-title">Excel批量导入</h1>
                <p class="page-subtitle">通过Excel表格快速批量导入客户信息</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-secondary">📥 下载导入模板</button>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">上传Excel文件</h3>
            </div>
            <div class="upload-zone">
                <div class="upload-icon">📊</div>
                <div class="upload-title">点击或拖拽Excel文件到此区域</div>
                <div class="upload-hint">支持 .xlsx 格式，单次最多导入 10,000 条数据</div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">导入历史</h3>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>文件名</th>
                        <th>导入时间</th>
                        <th>总行数</th>
                        <th>成功</th>
                        <th>失败</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>广交会客户名单.xlsx</td>
                        <td>2025-12-01 14:30</td>
                        <td>1,250</td>
                        <td style="color: var(--success);">1,200</td>
                        <td style="color: var(--error);">30</td>
                        <td><span class="status-badge success">✓ 已完成</span></td>
                        <td><a href="#" style="color: var(--primary);">查看日志</a></td>
                    </tr>
                    <tr>
                        <td>历史客户数据.xlsx</td>
                        <td>2025-11-28 09:15</td>
                        <td>856</td>
                        <td style="color: var(--success);">856</td>
                        <td style="color: var(--error);">0</td>
                        <td><span class="status-badge success">✓ 已完成</span></td>
                        <td><a href="#" style="color: var(--primary);">查看日志</a></td>
                    </tr>
                </tbody>
            </table>
        </div>
    `,

    // 邮件模板管理页面
    'email-template': `
        <div class="page-header">
            <div>
                <h1 class="page-title">邮件模板管理</h1>
                <p class="page-subtitle">创建和管理邮件模板，支持动态字段替换</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-primary" onclick="showTemplateEditor()">➕ 新建模板</button>
            </div>
        </div>

        <div class="card">
            <div class="filter-bar">
                <div class="filter-item">
                    <label>分类</label>
                    <select>
                        <option>全部</option>
                        <option>展会跟进</option>
                        <option>产品推广</option>
                        <option>节日问候</option>
                    </select>
                </div>
                <div class="filter-item">
                    <label>关键词</label>
                    <input type="text" placeholder="搜索模板名称...">
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;">
                ${generateTemplateCards()}
            </div>
        </div>
    `,

    // 批量发送页面
    'email-send': `
        <div class="page-header">
            <div>
                <h1 class="page-title">批量发送邮件</h1>
                <p class="page-subtitle">选择模板和客户，批量发送个性化邮件</p>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">第1步：选择邮件模板</h3>
            </div>
            <div class="form-group">
                <label class="form-label">选择模板</label>
                <select class="form-select">
                    <option>请选择邮件模板...</option>
                    <option selected>展会感谢信</option>
                    <option>新产品推广</option>
                    <option>节日问候</option>
                </select>
            </div>
            <div style="background: var(--gray-50); border-radius: var(--radius); padding: 20px; margin-top: 16px;">
                <div style="font-weight: 500; margin-bottom: 8px;">邮件预览</div>
                <div style="font-size: 14px; color: var(--gray-600);">
                    <strong>主题：</strong>感谢您在{{会面地点}}与我们会面<br><br>
                    尊敬的{{客户姓名}}先生/女士，<br><br>
                    感谢您在{{会面地点}}展会期间与我们会面。我们非常高兴能够与{{公司名称}}建立联系...
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">第2步：筛选收件人</h3>
                <span style="color: var(--gray-500);">已选择 <strong style="color: var(--primary);">45</strong> 位客户</span>
            </div>
            <div class="filter-bar">
                <div class="filter-item">
                    <label>优先级</label>
                    <select>
                        <option>全部</option>
                        <option selected>高优先级 (⭐⭐⭐⭐ 以上)</option>
                    </select>
                </div>
                <div class="filter-item">
                    <label>国家</label>
                    <select>
                        <option>全部</option>
                        <option>美国</option>
                        <option>德国</option>
                    </select>
                </div>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th><input type="checkbox" checked></th>
                        <th>客户姓名</th>
                        <th>邮箱</th>
                        <th>公司</th>
                        <th>优先级</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><input type="checkbox" checked></td>
                        <td>John Smith</td>
                        <td>john@abc-trading.com</td>
                        <td>ABC Trading Co.</td>
                        <td><span class="priority-stars">★★★★★</span></td>
                    </tr>
                    <tr>
                        <td><input type="checkbox" checked></td>
                        <td>Hans Mueller</td>
                        <td>hans@german-trucks.de</td>
                        <td>German Trucks GmbH</td>
                        <td><span class="priority-stars">★★★★★</span></td>
                    </tr>
                    <tr>
                        <td><input type="checkbox" checked></td>
                        <td>Marie Dupont</td>
                        <td>marie@france-auto.fr</td>
                        <td>France Auto SA</td>
                        <td><span class="priority-stars">★★★★☆</span></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">第3步：确认发送</h3>
            </div>
            <div style="display: flex; gap: 40px; margin-bottom: 20px;">
                <div>
                    <div style="font-size: 14px; color: var(--gray-500);">收件人数量</div>
                    <div style="font-size: 28px; font-weight: 700; color: var(--primary);">45</div>
                </div>
                <div>
                    <div style="font-size: 14px; color: var(--gray-500);">使用模板</div>
                    <div style="font-size: 28px; font-weight: 700; color: var(--gray-800);">展会感谢信</div>
                </div>
                <div>
                    <div style="font-size: 14px; color: var(--gray-500);">发送方式</div>
                    <div style="font-size: 28px; font-weight: 700; color: var(--gray-800);">立即发送</div>
                </div>
            </div>
            <button class="btn btn-primary" style="padding: 14px 40px;" onclick="showSendingProgress()">🚀 开始发送</button>
        </div>
    `,

    // 发送日志页面
    'email-logs': `
        <div class="page-header">
            <div>
                <h1 class="page-title">发送日志</h1>
                <p class="page-subtitle">查看邮件发送历史和详细状态</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-secondary">📥 导出日志</button>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">📧</div>
                <div class="stat-value">2,456</div>
                <div class="stat-label">累计发送</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">✅</div>
                <div class="stat-value">96.5%</div>
                <div class="stat-label">发送成功率</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">📨</div>
                <div class="stat-value">856</div>
                <div class="stat-label">本月发送</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">⚠️</div>
                <div class="stat-value">12</div>
                <div class="stat-label">发送失败</div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">发送任务列表</h3>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>任务名称</th>
                        <th>模板</th>
                        <th>发送时间</th>
                        <th>收件人</th>
                        <th>成功</th>
                        <th>失败</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>展会客户第一轮跟进</td>
                        <td>展会感谢信</td>
                        <td>2025-12-02 10:30</td>
                        <td>45</td>
                        <td style="color: var(--success);">43</td>
                        <td style="color: var(--error);">2</td>
                        <td><span class="status-badge success">✓ 已完成</span></td>
                        <td><a href="#" style="color: var(--primary);">详情</a></td>
                    </tr>
                    <tr>
                        <td>新产品推广</td>
                        <td>产品推介</td>
                        <td>2025-12-01 15:00</td>
                        <td>120</td>
                        <td style="color: var(--success);">118</td>
                        <td style="color: var(--error);">2</td>
                        <td><span class="status-badge success">✓ 已完成</span></td>
                        <td><a href="#" style="color: var(--primary);">详情</a></td>
                    </tr>
                    <tr>
                        <td>德国客户专项</td>
                        <td>展会感谢信</td>
                        <td>2025-11-30 09:00</td>
                        <td>28</td>
                        <td style="color: var(--success);">28</td>
                        <td style="color: var(--error);">0</td>
                        <td><span class="status-badge success">✓ 已完成</span></td>
                        <td><a href="#" style="color: var(--primary);">详情</a></td>
                    </tr>
                </tbody>
            </table>
        </div>
    `,

    // 系统设置页面
    'settings': `
        <div class="page-header">
            <div>
                <h1 class="page-title">系统设置</h1>
                <p class="page-subtitle">管理账号信息和SMTP邮箱配置</p>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 200px 1fr; gap: 24px;">
            <div class="card" style="padding: 0; height: fit-content;">
                <div style="padding: 12px;">
                    <a href="#" class="nav-item active" style="margin: 0;">
                        <span class="nav-icon">👤</span>
                        <span class="nav-text">个人资料</span>
                    </a>
                    <a href="#" class="nav-item" style="margin: 0;">
                        <span class="nav-icon">🔒</span>
                        <span class="nav-text">账号安全</span>
                    </a>
                    <a href="#" class="nav-item" style="margin: 0;">
                        <span class="nav-icon">📧</span>
                        <span class="nav-text">邮箱配置</span>
                    </a>
                    <a href="#" class="nav-item disabled" style="margin: 0;">
                        <span class="nav-icon">👥</span>
                        <span class="nav-text">团队管理</span>
                        <span class="nav-badge coming">即将推出</span>
                    </a>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">邮箱配置 (SMTP)</h3>
                    <button class="btn btn-primary">➕ 添加配置</button>
                </div>

                <div style="border: 1px solid var(--gray-200); border-radius: var(--radius-lg); padding: 20px; margin-bottom: 16px;">
                    <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 16px;">
                        <div>
                            <h4 style="font-size: 16px; margin-bottom: 4px;">公司邮箱 <span class="status-badge info">默认</span></h4>
                            <p style="font-size: 14px; color: var(--gray-500);">sales@company.com</p>
                        </div>
                        <span class="status-badge success">✓ 连接正常</span>
                    </div>
                    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; font-size: 14px; color: var(--gray-600);">
                        <div>
                            <span style="color: var(--gray-400);">SMTP服务器</span>
                            <div>smtp.company.com:465</div>
                        </div>
                        <div>
                            <span style="color: var(--gray-400);">加密方式</span>
                            <div>SSL/TLS</div>
                        </div>
                        <div>
                            <span style="color: var(--gray-400);">每日限额</span>
                            <div>500封/天</div>
                        </div>
                    </div>
                    <div style="margin-top: 16px; display: flex; gap: 12px;">
                        <button class="btn btn-secondary btn-sm">测试连接</button>
                        <button class="btn btn-secondary btn-sm">编辑</button>
                        <button class="btn btn-secondary btn-sm" style="color: var(--error);">删除</button>
                    </div>
                </div>
            </div>
        </div>
    `,

    // 功能路线图页面
    'roadmap': `
        <div class="page-header">
            <div>
                <h1 class="page-title">功能路线图</h1>
                <p class="page-subtitle">了解TruckTools的发展规划，订阅您感兴趣的功能</p>
            </div>
        </div>

        <div class="card">
            <div class="roadmap-timeline">
                <div class="timeline-item">
                    <div class="timeline-dot released"></div>
                    <div class="timeline-quarter">2025 Q4</div>
                    <div class="timeline-card">
                        <h4><span class="status-badge success">✓ 已上线</span> 客户管理模块</h4>
                        <p>名片OCR识别、客户信息管理、Excel批量导入导出</p>
                    </div>
                    <div class="timeline-card" style="margin-top: 12px;">
                        <h4><span class="status-badge success">✓ 已上线</span> 邮件营销模块</h4>
                        <p>邮件模板管理、批量发送、发送日志追踪</p>
                    </div>
                </div>

                <div class="timeline-item">
                    <div class="timeline-dot developing"></div>
                    <div class="timeline-quarter">2026 Q1</div>
                    <div class="timeline-card">
                        <h4><span class="status-badge warning">🔨 开发中</span> 邮件追踪增强</h4>
                        <p>邮件打开率、点击率追踪 • <a href="#" style="color: var(--primary);">128人感兴趣</a></p>
                    </div>
                    <div class="timeline-card" style="margin-top: 12px;">
                        <h4><span class="status-badge info">📅 计划中</span> 客户标签系统</h4>
                        <p>自定义标签、智能标签 • <a href="#" style="color: var(--primary);">订阅通知</a></p>
                    </div>
                </div>

                <div class="timeline-item">
                    <div class="timeline-dot planned"></div>
                    <div class="timeline-quarter">2026 Q2</div>
                    <div class="timeline-card">
                        <h4><span class="status-badge info">📅 计划中</span> 销售管理模块</h4>
                        <p>报价单管理、合同管理、订单管理、销售漏斗 • <a href="#" style="color: var(--primary);">89人感兴趣</a></p>
                    </div>
                </div>

                <div class="timeline-item">
                    <div class="timeline-dot planned"></div>
                    <div class="timeline-quarter">2026 Q3-Q4</div>
                    <div class="timeline-card">
                        <h4><span class="status-badge info">📅 计划中</span> 物流跟踪模块</h4>
                        <p>货运追踪、清关进度</p>
                    </div>
                    <div class="timeline-card" style="margin-top: 12px;">
                        <h4><span class="status-badge info">📅 计划中</span> 供应链管理</h4>
                        <p>供应商管理、采购管理、库存管理</p>
                    </div>
                </div>

                <div class="timeline-item">
                    <div class="timeline-dot planned"></div>
                    <div class="timeline-quarter">2027年</div>
                    <div class="timeline-card">
                        <h4><span class="status-badge info">📅 规划中</span> 数据分析模块</h4>
                        <p>销售报表、客户分析、业务预测</p>
                    </div>
                    <div class="timeline-card" style="margin-top: 12px;">
                        <h4><span class="status-badge info">📅 规划中</span> AI智能助手</h4>
                        <p>智能邮件生成、客户推荐、商机预测</p>
                    </div>
                </div>
            </div>
        </div>
    `,

    // 即将推出占位页
    'coming-soon': `
        <div class="coming-soon-page">
            <div class="coming-soon-icon">🚧</div>
            <h2 class="coming-soon-title">此功能即将推出</h2>
            <p class="coming-soon-desc">
                我们正在努力开发此功能，预计在未来版本中上线，敬请期待！<br>
                想了解更多？查看我们的功能路线图
            </p>
            <button class="btn btn-primary" onclick="showPage('roadmap')">🗺️ 查看功能路线图</button>
        </div>
    `
};

// 生成名片卡片项
function generateCardItems() {
    const cards = [
        { name: 'John Smith', company: 'ABC Trading Co.', email: 'john@abc-trading.com', priority: 5, confidence: 95 },
        { name: 'Hans Mueller', company: 'German Trucks GmbH', email: 'hans@german-trucks.de', priority: 5, confidence: 92 },
        { name: 'Marie Dupont', company: 'France Auto SA', email: 'marie@france-auto.fr', priority: 4, confidence: 88 }
    ];
    
    return cards.map(card => `
        <div style="border: 1px solid var(--gray-200); border-radius: var(--radius-lg); overflow: hidden;">
            <div style="height: 160px; background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%); display: flex; align-items: center; justify-content: center; font-size: 48px;">
                📇
            </div>
            <div style="padding: 16px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <span class="priority-stars">${'★'.repeat(card.priority)}${'☆'.repeat(5-card.priority)}</span>
                    <span class="status-badge success">${card.confidence}% 置信度</span>
                </div>
                <h4 style="font-size: 16px; margin-bottom: 4px;">${card.name}</h4>
                <p style="font-size: 13px; color: var(--gray-500); margin-bottom: 4px;">${card.company}</p>
                <p style="font-size: 13px; color: var(--gray-500); margin-bottom: 12px;">${card.email}</p>
                <div style="display: flex; gap: 8px;">
                    <button class="btn btn-primary btn-sm" style="flex: 1; padding: 8px;">✓ 确认</button>
                    <button class="btn btn-secondary btn-sm" style="flex: 1; padding: 8px;">✏️ 编辑</button>
                </div>
            </div>
        </div>
    `).join('');
}

// 生成客户行数据
function generateCustomerRows() {
    const customers = [
        { priority: 5, name: 'John Smith', email: 'john@abc-trading.com', company: 'ABC Trading Co.', country: '美国', date: '2025-11-28', source: 'ocr' },
        { priority: 5, name: 'Hans Mueller', email: 'hans@german-trucks.de', company: 'German Trucks GmbH', country: '德国', date: '2025-11-28', source: 'ocr' },
        { priority: 4, name: 'Marie Dupont', email: 'marie@france-auto.fr', company: 'France Auto SA', country: '法国', date: '2025-11-27', source: 'ocr' },
        { priority: 4, name: 'Yuki Tanaka', email: 'yuki@japan-motors.jp', company: 'Japan Motors Inc.', country: '日本', date: '2025-11-27', source: 'import' },
        { priority: 3, name: 'Carlos Rodriguez', email: 'carlos@mexico-trucks.mx', company: 'Mexico Trucks SA', country: '墨西哥', date: '2025-11-26', source: 'manual' }
    ];
    
    const sourceLabels = { ocr: '名片识别', import: 'Excel导入', manual: '手动录入' };
    
    return customers.map(c => `
        <tr>
            <td><input type="checkbox"></td>
            <td><span class="priority-stars">${'★'.repeat(c.priority)}${'☆'.repeat(5-c.priority)}</span></td>
            <td><a href="#" style="color: var(--primary); font-weight: 500;">${c.name}</a></td>
            <td>${c.email}</td>
            <td>${c.company}</td>
            <td>${c.country}</td>
            <td>${c.date}</td>
            <td><span class="status-badge info">${sourceLabels[c.source]}</span></td>
            <td>
                <a href="#" style="color: var(--primary); margin-right: 12px;">编辑</a>
                <a href="#" style="color: var(--error);">删除</a>
            </td>
        </tr>
    `).join('');
}

// 生成模板卡片
function generateTemplateCards() {
    const templates = [
        { name: '展会感谢信', category: '展会跟进', uses: 45, updated: '2025-12-01' },
        { name: '新产品推广', category: '产品推广', uses: 23, updated: '2025-11-28' },
        { name: '节日问候', category: '节日问候', uses: 12, updated: '2025-11-20' }
    ];
    
    return templates.map(t => `
        <div style="border: 1px solid var(--gray-200); border-radius: var(--radius-lg); padding: 20px;">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 16px;">
                <div>
                    <h4 style="font-size: 16px; margin-bottom: 4px;">${t.name}</h4>
                    <span class="status-badge info">${t.category}</span>
                </div>
                <span style="font-size: 24px;">📧</span>
            </div>
            <div style="font-size: 13px; color: var(--gray-500); margin-bottom: 16px;">
                使用次数: ${t.uses} · 更新于 ${t.updated}
            </div>
            <div style="display: flex; gap: 8px;">
                <button class="btn btn-primary btn-sm" style="flex: 1;">使用</button>
                <button class="btn btn-secondary btn-sm" style="flex: 1;">编辑</button>
                <button class="btn btn-secondary btn-sm">复制</button>
            </div>
        </div>
    `).join('');
}

// 显示登录页
function showLogin() {
    document.getElementById('login-page').classList.add('active');
    document.getElementById('main-app').classList.remove('active');
}

// 显示主应用
function showMainApp() {
    document.getElementById('login-page').classList.remove('active');
    document.getElementById('main-app').classList.add('active');
    showPage('card-upload');
}

// 显示注册
function showRegister() {
    alert('注册功能演示 - 实际产品中将显示注册表单');
}

// 切换侧边栏
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
}

// 显示页面
function showPage(pageName) {
    const mainContent = document.getElementById('main-content');
    
    // 处理"即将推出"的模块
    const comingSoonPages = ['dashboard', 'sales', 'logistics', 'supply', 'docs', 'analytics'];
    if (comingSoonPages.includes(pageName)) {
        mainContent.innerHTML = pageTemplates['coming-soon'];
        return;
    }
    
    // 更新侧边栏激活状态
    updateSidebarActive(pageName);
    
    // 渲染页面内容
    if (pageTemplates[pageName]) {
        mainContent.innerHTML = pageTemplates[pageName];
    }
}

// 更新侧边栏激活状态
function updateSidebarActive(pageName) {
    // 移除所有激活状态
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelectorAll('.submenu-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // 隐藏所有子菜单
    document.querySelectorAll('.nav-submenu').forEach(submenu => {
        submenu.classList.add('hidden');
    });
    
    // 根据页面名称激活对应菜单
    const customerPages = ['card-upload', 'excel-import', 'customer-list'];
    const emailPages = ['email-template', 'email-send', 'email-logs'];
    
    if (customerPages.includes(pageName)) {
        document.querySelector('[data-page="customers"]').classList.add('active');
        document.getElementById('customer-submenu').classList.remove('hidden');
    } else if (emailPages.includes(pageName)) {
        document.querySelector('[data-page="email"]').classList.add('active');
        document.getElementById('email-submenu').classList.remove('hidden');
    } else if (pageName === 'settings') {
        document.querySelector('[data-page="settings"]').classList.add('active');
    }
}

// 模拟上传
function simulateUpload() {
    alert('文件上传功能演示 - 实际产品中将打开文件选择器');
}

// 显示模板编辑器
function showTemplateEditor() {
    alert('邮件模板编辑器 - 实际产品中将打开富文本编辑器');
}

// 显示发送进度
function showSendingProgress() {
    alert('邮件发送中...\n\n已发送: 15/45\n成功: 14\n失败: 1\n\n(演示模式)');
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 默认显示登录页
    showLogin();
});

