import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const GitHubCallback = () => {
    const navigate = useNavigate();

    useEffect(() => {
        // 获取 URL 中的查询参数
        const queryParams = new URLSearchParams(window.location.search);
        const code = queryParams.get("code"); // GitHub 返回的授权码

        if (code) {
            // 将授权码发送到后端以完成登录
            axios
                .post("http://localhost:8070/api/oauth/callback", { code })
                .then((response) => {
                    if (response.data.status === 0) {
                        // 保存令牌到本地存储
                        localStorage.setItem("token", response.data.token);
                        alert("登录成功！");
                        navigate("/catalog/index"); // 跳转到主页
                    } else {
                        alert("登录失败：" + response.data.msg);
                    }
                })
                .catch((error) => {
                    console.error("GitHub 登录失败：", error);
                    alert("登录过程中发生错误，请稍后再试！");
                });
        } else {
            alert("未获取到授权码！");
            navigate("/account/login"); // 跳转回登录页面
        }
    }, [navigate]);

    return <div>正在处理登录，请稍候...</div>;
};

export default GitHubCallback;