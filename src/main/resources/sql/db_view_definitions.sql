
-- standard_variable_summary view : Standard variable summary information modelled as per http://gmod.org/wiki/Chado_Tables#Table:_cvterm
	-- Definitin of this view is availabe at https://github.com/digitalabs/IBDBScripts/blob/master/local/common/10_IBDBV2-DMS-Views_20140707.sql
		
-- view for germplasm search
		
drop view if exists germplasm_summary;

	create view germplasm_summary as
	
		select gp.gid, group_concat(n.nval) as names, ld.listid
		
		from germplsm gp 
			
			inner join names n on n.gid = gp.gid
			left outer join listdata ld on ld.gid = n.gid
		
		where gp.gid != gp.grplce and n.nstat != 9
		group by gid
	;
	

-- germplasm_trial_details view: 
	-- Definitin of this view is availabe at https://github.com/digitalabs/IBDBScripts/blob/master/central/common/13_Breeders_Query_Views.sql
