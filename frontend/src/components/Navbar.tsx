import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import type { OnlineStats } from '../types/auth';
import { MessageSquare, Users, History, LogOut, LogIn, UserPlus, Shield } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { isGuest, displayName, isAuthenticated, logout } = useAuth();
  const [stats, setStats] = useState<OnlineStats>({ onlineUsers: 1, activeRooms: 0, queueLength: 0 });
  const navigate = useNavigate();

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await api.getOnlineStats();
        setStats(data);
      } catch {
        // Fallback or ignore
      }
    };

    fetchStats();
    const interval = setInterval(fetchStats, 10000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-40 w-full glass-panel border-b border-white/10 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-2.5 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 p-0.5 shadow-lg shadow-indigo-500/20 group-hover:scale-105 transition-transform">
            <div className="w-full h-full bg-[#0d121f] rounded-[10px] flex items-center justify-center">
              <MessageSquare className="w-5 h-5 text-indigo-400" />
            </div>
          </div>
          <div>
            <span className="font-extrabold text-xl tracking-tight bg-gradient-to-r from-white via-slate-100 to-indigo-200 bg-clip-text text-transparent">
              Stranger<span className="text-indigo-400">Chat</span>
            </span>
          </div>
        </Link>

        {/* Center Live Stats Badge */}
        <div className="hidden sm:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900/80 border border-slate-700/60 shadow-inner">
          <span className="relative flex h-2.5 w-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
          </span>
          <span className="text-xs font-medium text-slate-300 flex items-center gap-1.5">
            <Users className="w-3.5 h-3.5 text-slate-400" />
            <strong className="text-white">{stats.onlineUsers}</strong> online now
          </span>
        </div>

        {/* Right Nav Actions */}
        <div className="flex items-center gap-3">
          {isAuthenticated && !isGuest ? (
            <>
              <Link
                to="/history"
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-white/5 transition-colors"
                title="My Chat History"
              >
                <History className="w-4 h-4 text-indigo-400" />
                <span className="hidden md:inline">My Chats</span>
              </Link>
              <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-indigo-950/40 border border-indigo-500/20 text-xs font-semibold text-indigo-300">
                <span className="w-2 h-2 rounded-full bg-indigo-400"></span>
                {displayName}
              </div>
              <button
                onClick={() => {
                  logout();
                  navigate('/');
                }}
                className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                title="Log Out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </>
          ) : isGuest ? (
            <>
              <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800/60 border border-slate-700/50 text-xs font-medium text-slate-300">
                <Shield className="w-3.5 h-3.5 text-amber-400" />
                <span className="text-slate-400">Guest:</span> {displayName}
              </div>
              <Link
                to="/login"
                className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition-colors"
              >
                Sign In
              </Link>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="flex items-center gap-1 px-3 py-1.5 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-white/5 transition-colors"
              >
                <LogIn className="w-4 h-4" />
                <span>Log In</span>
              </Link>
              <Link
                to="/register"
                className="flex items-center gap-1 px-3.5 py-1.5 rounded-lg text-sm font-semibold bg-indigo-600 hover:bg-indigo-500 text-white shadow-md shadow-indigo-600/30 transition-all hover:scale-[1.02]"
              >
                <UserPlus className="w-4 h-4" />
                <span>Sign Up</span>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
