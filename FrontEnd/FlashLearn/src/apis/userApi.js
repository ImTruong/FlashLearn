// fake data
import userModule from "@/store/modules/userModule";
import store from "@/store";
// end fake data
import axios from "axios"

export const login = async (username,password) => {
    try {
        const response = await axios.post('user/login', {
            username,
            password,
        });

        if (response.status === 200) {
            return response.data.data;
        }

        throw new Error(response.data.message || "Login failed");
    } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
            throw new Error(error.response.data.message); // Ném lỗi lên trên
        } else {
            throw new Error("Something went wrong");
        }
    }
    // const { fakeDataUserList } = store.state.userModule;
    // const user = fakeDataUserList.find(user => user.username === username && user.password === password);
    // if(user)
    //     return "fakeToken";
    // else
    //     throw new Error("Invalid username or password");
}

export const register = async (registerRequest) => {
    try {
        const response = await axios.post('user/register', {
            fullName: registerRequest.fullname,
            username: registerRequest.username,
            password: registerRequest.password,
            email: registerRequest.email,
            country: registerRequest.country
        });
        return response.data.message;
    } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
            console.log("Error message:", error.response.data.message);
            return error.response.data.message;
        } else {
            console.log("Unexpected error:", error);
            throw new Error("Something went wrong");
        }
    }
    // const { fakeDataUserList } = store.state.userModule;
    // const user = fakeDataUserList.find(user => user.username === registerRequest.username || user.email === registerRequest.email);
    // if(user)
    //     throw new Error("Username or Email already exists");
    // else{
    //     store.dispatch('userModule/addUser', {
    //         username: registerRequest.username,
    //         email: registerRequest.email,
    //         password: registerRequest.password,
    //         fullName: registerRequest.fullname,
    //         country: registerRequest.country
    //     });
    //     return "Register successfully"
    // }
}

export const changePassword = async (changePasswordRequest,token) => {
    try {
        const response = await axios.put('user/password', changePasswordRequest, {
            headers: {
            Authorization: `Bearer ${token}`,
            },
        });

        return response.data.message;
    } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
            console.log("Error message:", error.response.data.message);
            return error.response.data.message;
        } else {
            console.log("Unexpected error:", error);
            throw new Error("Something went wrong");
        }
    }
    // const { fakeDataUserList } = store.state.userModule;
    // const user = fakeDataUserList.find(user => user.username === "admin" && user.password === changePasswordRequest.currentPassword);
    // if(user){
    //     if (changePasswordRequest.newPassword !== changePasswordRequest.confirmPassword)
    //         throw new Error("New password and confirm password do not match");
    //     store.dispatch('userModule/changePassword', {
    //         password: changePasswordRequest.newPassword
    //     });
    //     return "Change password successfully";
    // }
    // else{
    //     throw new Error("Wrong old password");
    // }
}

export const updateUserInfo = async (updateUserInfoRequest,token) => {
    try {

        const response = await axios.put('user', updateUserInfoRequest, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        return response.data.message;
    } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
            console.log("Error message:", error.response.data.message);
            return error.response.data.message;
        } else {
            console.log("Unexpected error:", error);
            throw new Error("Something went wrong");
        }
    }
    // const { fakeDataUserList } = store.state.userModule;
    // const user = fakeDataUserList.find(user => user.username === "admin");
    // if(user){
    //     store.dispatch('userModule/updateUserInfo', {
    //         email: updateUserInfoRequest.email,
    //         fullName: updateUserInfoRequest.fullName,
    //         country: updateUserInfoRequest.country
    //     });
    //     return "Update user info successfully";
    // }
    // else{
    //     throw new Error("Update user info failed");
    // }
}

export const getCurrentUser = async (token) => {
    try {
        const response = await axios.get('user', {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        return response.data.data;
    } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
            console.log("Error message:", error.response.data.message);
            return error.response.data.message;
        } else {
            console.log("Unexpected error:", error);
            throw new Error("Something went wrong");
        }
    }
    // const { fakeDataUserList } = store.state.userModule;
    // const user = fakeDataUserList.find(user => user.username === "admin");
    // if(user)
    //     return user;
    // else
    //     throw new Error("Get user info failed");
};

 