// API Configuration
const API_BASE_URL = 'http://localhost:8081/api';

// Store temporary data
let tempSignupData = {};
let tempLoginSic = '';

// DOM Elements
const tabBtns = document.querySelectorAll('.tab-btn');
const tabContents = document.querySelectorAll('.tab-content');
const loginForm = document.getElementById('loginForm');
const signupForm = document.getElementById('signupForm');
const loginOtpForm = document.getElementById('loginOtpForm');
const signupOtpForm = document.getElementById('signupOtpForm');
const authMessage = document.getElementById('authMessage');

// Tab Switching
tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        const tabName = btn.dataset.tab;
        
        // Update active tab button
        tabBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        // Update active tab content
        tabContents.forEach(content => {
            content.classList.remove('active');
            if (content.id === `${tabName}-tab`) {
                content.classList.add('active');
            }
        });
        
        // Reset forms and hide OTP sections
        resetForms();
    });
});

// Login Form Submission
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const sic = document.getElementById('loginSic').value.trim();
    
    if (!sic) {
        showMessage('Please enter your SIC', 'error');
        return;
    }
    
    setLoading(loginForm, true);
    
    try {
        // Check if SIC exists in database
        const response = await fetch(`${API_BASE_URL}/students/${sic}`);
        
        if (response.ok) {
            const student = await response.json();
            
            // Send OTP to registered email
            const otpResponse = await fetch(`${API_BASE_URL}/auth/send-login-otp`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    sic: sic,
                    email: student.email
                })
            });
            
            if (otpResponse.ok) {
                tempLoginSic = sic;
                document.getElementById('loginOtpSection').style.display = 'block';
                loginForm.style.display = 'none';
                showMessage(`Login OTP sent to ${student.email}`, 'success');
            } else {
                showMessage('Failed to send OTP. Please try again.', 'error');
            }
        } else {
            showMessage('SIC not found in our database. Please sign up first.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Network error. Please check if the backend server is running.', 'error');
    } finally {
        setLoading(loginForm, false);
    }
});

// Login OTP Verification
loginOtpForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const otp = document.getElementById('loginOtp').value.trim();
    
    if (!otp || otp.length !== 6) {
        showMessage('Please enter a valid 6-digit OTP', 'error');
        return;
    }
    
    setLoading(loginOtpForm, true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/verify-login-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                sic: tempLoginSic,
                otp: otp
            })
        });
        
        if (response.ok) {
            // Store session
            sessionStorage.setItem('userSic', tempLoginSic);
            sessionStorage.setItem('userType', 'student');
            
            showMessage('Login successful! Redirecting...', 'success');
            setTimeout(() => {
                window.location.href = 'user-dashboard.html';
            }, 1500);
        } else {
            showMessage('Invalid OTP. Please try again.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Verification failed. Please try again.', 'error');
    } finally {
        setLoading(loginOtpForm, false);
    }
});

// Signup Form Submission - Show Disclaimer First
signupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const sic = document.getElementById('signupSic').value.trim();
    const email = document.getElementById('signupEmail').value.trim();
    
    if (!sic || !email) {
        showMessage('Please fill in all required fields', 'error');
        return;
    }
    
    // Store data temporarily and show disclaimer
    tempSignupData = { sic, email };
    showDisclaimerModal();
});

// Disclaimer Modal Elements
const disclaimerModal = document.getElementById('disclaimerModal');
const disclaimerAgree = document.getElementById('disclaimerAgree');
const disclaimerContinue = document.getElementById('disclaimerContinue');
const disclaimerCancel = document.getElementById('disclaimerCancel');
const disclaimerContent = document.querySelector('.disclaimer-content');

// Show Disclaimer Modal
function showDisclaimerModal() {
    disclaimerModal.classList.add('active');
    disclaimerAgree.checked = false;
    disclaimerAgree.disabled = true; // Disable checkbox initially
    disclaimerContinue.disabled = true;
    
    // Add scroll listener
    disclaimerContent.addEventListener('scroll', checkScrollPosition);
    
    // Check initial scroll position
    checkScrollPosition();
}

// Check if user has scrolled to bottom
function checkScrollPosition() {
    const scrollTop = disclaimerContent.scrollTop;
    const scrollHeight = disclaimerContent.scrollHeight;
    const clientHeight = disclaimerContent.clientHeight;
    
    // Enable checkbox if scrolled to bottom (with 10px tolerance)
    if (scrollTop + clientHeight >= scrollHeight - 10) {
        disclaimerAgree.disabled = false;
    }
}

// Disclaimer Checkbox Handler
disclaimerAgree.addEventListener('change', (e) => {
    disclaimerContinue.disabled = !e.target.checked;
});

// Disclaimer Cancel
disclaimerCancel.addEventListener('click', () => {
    disclaimerModal.classList.remove('active');
    disclaimerContent.removeEventListener('scroll', checkScrollPosition);
    tempSignupData = {};
});

// Disclaimer Continue - Send OTP
disclaimerContinue.addEventListener('click', async () => {
    disclaimerModal.classList.remove('active');
    
    setLoading(signupForm, true);
    
    try {
        // Send verification OTP
        const response = await fetch(`${API_BASE_URL}/auth/send-signup-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                sic: tempSignupData.sic,
                email: tempSignupData.email
            })
        });
        
        if (response.ok) {
            document.getElementById('verifyEmail').textContent = tempSignupData.email;
            document.getElementById('signupOtpSection').style.display = 'block';
            signupForm.style.display = 'none';
            showMessage(`Verification OTP sent to ${tempSignupData.email}`, 'success');
        } else {
            const errorText = await response.text();
            showMessage(errorText || 'This SIC or email may already be registered.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Network error. Please check if the backend server is running.', 'error');
    } finally {
        setLoading(signupForm, false);
    }
});

// Close modal on outside click
window.addEventListener('click', (e) => {
    if (e.target === disclaimerModal) {
        disclaimerModal.classList.remove('active');
        tempSignupData = {};
    }
});

// Signup OTP Verification
signupOtpForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const otp = document.getElementById('signupOtp').value.trim();
    
    if (!otp || otp.length !== 6) {
        showMessage('Please enter a valid 6-digit OTP', 'error');
        return;
    }
    
    setLoading(signupOtpForm, true);
    
    try {
        // Verify OTP and create account
        const response = await fetch(`${API_BASE_URL}/students/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                sic: tempSignupData.sic,
                email: tempSignupData.email,
                otp: otp,
                status: 'ACTIVE'
            })
        });
        
        if (response.ok) {
            showMessage('Signup successful! Redirecting to login...', 'success');
            setTimeout(() => {
                // Switch to login tab
                document.querySelector('[data-tab="login"]').click();
                document.getElementById('loginSic').value = tempSignupData.sic;
            }, 2000);
        } else {
            showMessage('Invalid OTP or registration failed. Please try again.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Registration failed. Please try again.', 'error');
    } finally {
        setLoading(signupOtpForm, false);
    }
});

// Resend OTP Buttons
document.getElementById('resendLoginOtp').addEventListener('click', () => {
    document.getElementById('loginOtp').value = '';
    loginForm.querySelector('button[type="submit"]').click();
});

document.getElementById('resendSignupOtp').addEventListener('click', () => {
    document.getElementById('signupOtp').value = '';
    // Show disclaimer again before resending
    showDisclaimerModal();
});

// Helper Functions
function showMessage(message, type) {
    authMessage.textContent = message;
    authMessage.className = `message-box ${type}`;
    authMessage.style.display = 'block';
    
    setTimeout(() => {
        authMessage.style.display = 'none';
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

function resetForms() {
    loginForm.reset();
    signupForm.reset();
    loginForm.style.display = 'block';
    signupForm.style.display = 'block';
    document.getElementById('loginOtpSection').style.display = 'none';
    document.getElementById('signupOtpSection').style.display = 'none';
    authMessage.style.display = 'none';
}
