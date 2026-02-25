import request from '../utils/request';

export const testPing = () => {
    return request({
        url: '/api/ping',
        method: 'get',
    });
};