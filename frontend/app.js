// app.js

document.addEventListener('DOMContentLoaded', () => {
    // UI Elements
    const modeToggle = document.getElementById('modeToggle');
    const statusIndicator = document.getElementById('statusIndicator');
    const statusText = document.getElementById('statusText');
    const inputNumber = document.getElementById('inputNumber');
    const computeBtn = document.getElementById('computeBtn');
    const clearCacheBtn = document.getElementById('clearCacheBtn');
    const resetStatsBtn = document.getElementById('resetStatsBtn');
    const cacheVisualizer = document.getElementById('cacheVisualizer');
    const activityLog = document.getElementById('activityLog');
    const resultCard = document.getElementById('resultCard');
    const resultInput = document.getElementById('resultInput');
    const resultOutput = document.getElementById('resultOutput');
    const resultBadge = document.getElementById('resultBadge');
    const resultTime = document.getElementById('resultTime');

    // Stats Elements
    const statRequests = document.getElementById('statRequests');
    const statHitRatio = document.getElementById('statHitRatio');
    const statHits = document.getElementById('statHits');
    const statMisses = document.getElementById('statMisses');
    const statEvictions = document.getElementById('statEvictions');
    const statSize = document.getElementById('statSize');
    const visualCapacity = document.getElementById('visualCapacity');

    // Setup eviction listener (Demo Mode fallback logger)
    ApiService.setEvictionListener((key, value) => {
        logEvent('Evict', `Key = ${key} (Value = ${value})`, 'text-rose-600 bg-rose-50 border-rose-100');
    });

    // Check backend connection regularly
    let connectionCheckInterval;
    
    async function updateStatusBadge() {
        const mode = modeToggle.value;
        ApiService.setMode(mode);

        if (mode === 'demo') {
            statusIndicator.className = "flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold bg-blue-50 text-blue-600 border border-blue-100";
            statusIndicator.querySelector('span').className = "h-2 w-2 rounded-full bg-blue-500 animate-pulse";
            statusText.textContent = "Demo Cache (Local)";
            return;
        }

        // Live connection check
        const isOnline = await ApiService.checkConnection();
        if (isOnline) {
            statusIndicator.className = "flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold bg-emerald-50 text-emerald-600 border border-emerald-100";
            statusIndicator.querySelector('span').className = "h-2 w-2 rounded-full bg-emerald-500 animate-pulse";
            statusText.textContent = "Backend Connected (Port 6000)";
        } else {
            statusIndicator.className = "flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold bg-rose-50 text-rose-600 border border-rose-100";
            statusIndicator.querySelector('span').className = "h-2 w-2 rounded-full bg-rose-500 animate-pulse";
            statusText.textContent = "Backend Offline (Port 6000)";
            
            // Fallback user alert log
            logEvent('Warn', 'REST API server at port 6000 is offline. Please make sure the Java server is running.', 'text-rose-600 bg-rose-50 border-rose-100');
        }
    }

    // Logging helper
    function logEvent(type, message, classes = 'text-slate-600 bg-slate-50 border-slate-100') {
        const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        
        // Remove empty placeholder
        if (activityLog.innerHTML.includes('No activity recorded')) {
            activityLog.innerHTML = '';
        }

        const logItem = document.createElement('div');
        logItem.className = `flex items-start justify-between border rounded-xl px-3 py-2 text-xs font-medium ${classes} transition-all duration-300`;
        logItem.innerHTML = `
            <div class="flex gap-2">
                <span class="font-bold uppercase tracking-wider">[${type}]</span>
                <span>${message}</span>
            </div>
            <span class="text-[10px] text-slate-400 font-mono">${time}</span>
        `;
        activityLog.insertBefore(logItem, activityLog.firstChild);
    }

    // Refresh dashboard UI (viz + statistics)
    async function refreshUI() {
        try {
            // 1. Get cache contents
            const contents = await ApiService.getContents();
            cacheVisualizer.innerHTML = '';
            
            if (!contents || contents.length === 0) {
                cacheVisualizer.innerHTML = '<div class="flex-1 text-center py-8 text-sm text-slate-400 font-medium">Cache is currently empty</div>';
            } else {
                contents.forEach((entry, index) => {
                    const card = document.createElement('div');
                    card.className = "flex items-center gap-2";
                    
                    let separatorHtml = '';
                    if (index < contents.length - 1) {
                        separatorHtml = `
                            <svg class="h-4 w-4 text-slate-300 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                            </svg>
                        `;
                    }

                    card.innerHTML = `
                        <div class="bg-white border border-slate-200 rounded-xl px-4 py-2.5 shadow-sm text-center min-w-[85px] hover:border-blue-400 hover:shadow-md hover:scale-105 transition-all">
                            <div class="text-[9px] font-bold text-slate-400 uppercase tracking-wide">Key: ${entry.key}</div>
                            <div class="text-sm font-extrabold text-blue-600 mt-0.5">${entry.value}</div>
                        </div>
                        ${separatorHtml}
                    `;
                    cacheVisualizer.appendChild(card);
                });
            }

            // 2. Get statistics
            const stats = await ApiService.getStatistics();
            statRequests.textContent = stats.totalRequests;
            statHitRatio.textContent = `${stats.hitRatio}%`;
            statHits.textContent = stats.hits;
            statMisses.textContent = stats.misses;
            statEvictions.textContent = stats.evictions;
            statSize.textContent = `${stats.currentSize} / ${stats.capacity}`;
            visualCapacity.textContent = stats.capacity;

        } catch (e) {
            console.error(e);
            logEvent('Error', 'Failed to retrieve cache status or statistics: ' + e.message, 'text-rose-600 bg-rose-50 border-rose-100');
        }
    }

    // Set buttons loading states
    function setLoading(loading) {
        computeBtn.disabled = loading;
        clearCacheBtn.disabled = loading;
        resetStatsBtn.disabled = loading;
        inputNumber.disabled = loading;
        
        if (loading) {
            computeBtn.innerHTML = `
                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white inline-block" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg> Computing...
            `;
            computeBtn.className = "rounded-xl bg-blue-400 px-6 py-3 font-semibold text-white cursor-not-allowed shadow-sm";
        } else {
            computeBtn.innerHTML = "Compute";
            computeBtn.className = "rounded-xl bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-700 active:bg-blue-800 shadow-sm shadow-blue-100 transition-all cursor-pointer";
        }
    }

    // Handle Compute
    async function handleCompute() {
        const val = inputNumber.value.trim();
        if (!val) return;

        const number = parseInt(val);
        if (isNaN(number)) {
            alert("Please enter a valid integer");
            return;
        }

        setLoading(true);
        resultCard.classList.add('hidden'); // Hide previous result

        try {
            logEvent('Action', `Requested calculation for input = ${number}`);
            
            const res = await ApiService.compute(number);
            
            // Render result card
            resultInput.textContent = res.input;
            resultOutput.textContent = res.result;
            resultTime.textContent = `${res.executionTimeMs} ms`;
            
            if (res.status === 'HIT') {
                resultCard.className = "rounded-2xl border border-emerald-200 bg-emerald-50/30 p-6 shadow-sm shadow-emerald-50/50 transition-all duration-300";
                resultBadge.className = "rounded-full bg-emerald-100 px-3.5 py-1.5 text-xs font-extrabold tracking-wide uppercase text-emerald-700";
                resultBadge.textContent = "Cache HIT";
                logEvent('HIT', `Result = ${res.result} (fetched instantly in ${res.executionTimeMs}ms)`, 'text-emerald-700 bg-emerald-50 border-emerald-100');
            } else {
                resultCard.className = "rounded-2xl border border-amber-200 bg-amber-50/30 p-6 shadow-sm shadow-amber-50/50 transition-all duration-300";
                resultBadge.className = "rounded-full bg-amber-100 px-3.5 py-1.5 text-xs font-extrabold tracking-wide uppercase text-amber-700";
                resultBadge.textContent = "Cache MISS";
                logEvent('MISS', `Result = ${res.result} computed (took ${res.executionTimeMs}ms)`, 'text-amber-700 bg-amber-50 border-amber-100');
            }
            
            resultCard.classList.remove('hidden');
            inputNumber.value = '';

        } catch (e) {
            console.error(e);
            logEvent('Error', 'Computation failed: ' + e.message, 'text-rose-600 bg-rose-50 border-rose-100');
            alert('Failed to execute compute: ' + e.message);
        } finally {
            setLoading(false);
            refreshUI();
        }
    }

    // Bind Event Listeners
    computeBtn.addEventListener('click', handleCompute);
    
    inputNumber.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            handleCompute();
        }
    });

    clearCacheBtn.addEventListener('click', async () => {
        if (confirm("Are you sure you want to clear all cache contents?")) {
            try {
                await ApiService.clear();
                logEvent('Cleared', 'Cache has been cleared.', 'text-slate-600 bg-slate-50 border-slate-100');
                resultCard.classList.add('hidden');
                refreshUI();
            } catch (e) {
                alert("Failed to clear cache: " + e.message);
            }
        }
    });

    resetStatsBtn.addEventListener('click', async () => {
        if (confirm("Are you sure you want to reset all performance statistics?")) {
            try {
                await ApiService.resetStats();
                logEvent('Reset', 'Statistics have been reset.', 'text-slate-600 bg-slate-50 border-slate-100');
                refreshUI();
            } catch (e) {
                alert("Failed to reset statistics: " + e.message);
            }
        }
    });

    modeToggle.addEventListener('change', async () => {
        setLoading(true);
        resultCard.classList.add('hidden');
        await updateStatusBadge();
        setLoading(false);
        refreshUI();
    });

    // Initialize Dashboard
    updateStatusBadge().then(() => refreshUI());
    
    // Check connection every 5 seconds if live mode is selected
    connectionCheckInterval = setInterval(() => {
        if (modeToggle.value === 'live') {
            updateStatusBadge().then(() => refreshUI());
        }
    }, 5000);
});
