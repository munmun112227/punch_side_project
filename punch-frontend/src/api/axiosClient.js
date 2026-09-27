import axios from 'axios';

const axiosClient = axios.create({
    baseURL: '',
    timeout: 5000,
});

axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.data && error.response.data.error) {
            alert(error.response.data.error);
        } else {
            alert("與後台溝通產生例外狀況，請通知系統管理員；錯誤碼: 601");
        }
        return Promise.reject(error);
    }
);

export default axiosClient;
