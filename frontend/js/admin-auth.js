// API Configuration
const API_BASE_URL = 'https://hostelattendancesystem.onrender.com/api';
const ADMIN_EMAIL = 'experimentpurpose1@gmail.com';

// DOM Elements
const adminLoginForm = document.getElementById('adminLoginForm');
const adminOtpForm = document.getElementById('adminOtpForm');
const adminAuthMessage = document.getElementById('adminAuthMessage');

// Admin Login Form Submission
adminLoginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    setLoading(adminLoginForm, true);
    
    try {
        // Send OTP to admin email
        const response = await fetch(`${API_BASE_URL}/auth/send-admin-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: ADMIN_EMAIL
            })
        });
        
        if (response.ok) {
            document.getElementById('adminOtpSection').style.display = 'block';
            adminLoginForm.style.display = 'none';
            showMessage(`Admin verification OTP sent to ${ADMIN_EMAIL}`, 'success');
        } else {
            showMessage('Failed to send OTP. Please try again.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Network error. Please check if the backend server is running.', 'error');
    } finally {
        setLoading(adminLoginForm, false);
    }
});

// Admin OTP Verification
adminOtpForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const otp = document.getElementById('adminOtp').value.trim();
    
    if (!otp || otp.length !== 6) {
        showMessage('Please enter a valid 6-digit OTP', 'error');
        return;
    }
    
    setLoading(adminOtpForm, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/verify-admin-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: ADMIN_EMAIL,
                otp: otp
            })
        });
        
        if (response.ok) {
            // Store admin session
            sessionStorage.setItem('adminAuth', 'true');
            sessionStorage.setItem('userType', 'admin');
            sessionStorage.setItem('adminEmail', ADMIN_EMAIL);
            
            showMessage('Verification successful! Redirecting to admin panel...', 'success');
            setTimeout(() => {
                window.location.href = 'admin-dashboard.html';
            }, 1500);
        } else {
            showMessage('Invalid OTP. Please try again.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Verification failed. Please try again.', 'error');
    } finally {
        setLoading(adminOtpForm, false);
    }
});

// Resend Admin OTP
document.getElementById('resendAdminOtp').addEventListener('click', () => {
    document.getElementById('adminOtp').value = '';
    adminLoginForm.querySelector('button[type="submit"]').click();
});

// Helper Functions
function showMessage(message, type) {
    adminAuthMessage.textContent = message;
    adminAuthMessage.className = `message-box ${type}`;
    adminAuthMessage.style.display = 'block';
    
    setTimeout(() => {
        adminAuthMessage.style.display = 'none';
    }, 5000);
}

function setLoading(form, isLoading) {
    const btn = form.querySelector('button[type="submit"]');
    const btnText = btn.querySelector('.btn-text');
    const btnLoader = btn.querySelector('.btn-loader');
    
    if (isLoading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';
        btn.disabled = true;
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        btn.disabled = false;
    }
}
